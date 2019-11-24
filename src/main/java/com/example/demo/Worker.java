package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Worker extends Thread {

	int id;

	public Worker(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		for (int key = 0; key < 10; key++) {
			doJobWithLock(key);
		}
	}

	private void doJobWithLock(int key) {
		try (AutoReleasableLock lock = SimpleFileLock.getLockForKey(key)) {
			log(key, id, "processed " + lock);
			sleepMs(randomTime());
		} catch (LockFailedException le) {
			log(key, id, "lock_failed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private AutoReleasableLock getSimpleFileLockFor(int key) throws LockFailedException {
		if (new Random().nextInt(10) < 2)
			throw new LockFailedException();
		return new MyLock(id, key);
	}

	private void sleepMs(long randomTime) {
		try {
			Thread.sleep(randomTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private long randomTime() {
		Random random = new Random();
		return random.nextInt(10) * 100L;
	}

	public static List<Worker> newWorkers(int cnt) {
		List<Worker> workers = new ArrayList<>();
		for (int i = 0; i < cnt; i++)
			workers.add(new Worker(i));
		return workers;
	}

	private void log(int key, int id, String s) {
		System.out.println(key + ":" + id + " " + s);
	}
}
