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
		if (fileExists(key))
			throw new LockFailedException();
		try {
			boolean ok = createFile(key);
			if (!ok)
				throw new LockFailedException("fail to create lock file");
		} catch (IOException e) {
			throw new LockFailedException(e);
		}
		return new SimpleFileLock(key);
	}

	@Override
	public void close() throws Exception {
		deleteFile(key);
	}

	private static boolean fileExists(int key) {
		new File(DIR).mkdir();
		File file = new File(DIR, key + "");
		return file.exists();
	}

	private static boolean createFile(int key) throws IOException {
		File file = new File(DIR, key + "");
		file.deleteOnExit();
		return file.createNewFile();
	}

	private void deleteFile(int key) {
		new File(DIR, key + "").delete();
	}

	@Override
	public String toString() {
		return "SimpleFileLock(key:" + key + ")";
	}
}
