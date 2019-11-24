package com.example.demo;

import java.io.File;
import java.io.IOException;

public class SimpleFileLock implements AutoReleasableLock {

	private static final String DIR = "/tmp/lock.test";

	private int key;

	public SimpleFileLock(int key) {
		this.key = key;
	}

	public static AutoReleasableLock getLockForKey(int key) throws LockFailedException {
		if (fileExists_processing_or_done(key))
			throw new LockFailedException();
		try {
			Thread.sleep(500);
			boolean ok = createFile_processing(key);
			if (!ok)
				throw new LockFailedException("fail to create lock file");
		} catch (IOException | InterruptedException e) {
			throw new LockFailedException(e);
		}
		return new SimpleFileLock(key);
	}

	@Override
	public void close() throws Exception {
		renameProcessingToDone(key);
	}

	private static boolean fileExists_processing_or_done(int key) {
		new File(DIR).mkdir();
		return new File(DIR, key + ".processing").exists()
				|| new File(DIR, key + ".done").exists()
				;
	}

	private static boolean createFile_processing(int key) throws IOException {
		File file = new File(DIR, key + ".processing");
		file.deleteOnExit();
		return file.createNewFile();
	}

	private void renameProcessingToDone(int key) {
		new File(DIR, key + ".processing").renameTo(new File(DIR, key + ".done"));
	}

	@Override
	public String toString() {
		return "SimpleFileLock(key:" + key + ")";
	}
}
