package com.gimslab.redisexam.simpledistributedlock;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Worker extends Thread {

	private int id;
	private StringRedisTemplate stringRedisTemplate;

	private Worker(int id, StringRedisTemplate stringRedisTemplate) {
		this.id = id;
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Override
	public void run() {
		for (int key = 0; key < 10; key++)
			doJobWithLock(key);
	}

	private void doJobWithLock(int key) {
		if (jobFileDone(key).exists()) {
			log(key, id, "found job file done. skip");
			return;
		}
		try (AutoReleasableLock lock = RedisSimpleLock.getLockForKey(key, stringRedisTemplate)) {
			log(key, id, "processing " + lock);
			sleepMs(randomTime());
			boolean ok = jobFileDone(key).createNewFile();
			if (!ok)
				throw new RuntimeException("job failed");
		} catch (LockFailedException le) {
			log(key, id, "lock_failed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File jobFileDone(int key) {
		return new File("/tmp/lock.test", key + ".done");
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

	static List<Worker> newWorkers(int cnt, StringRedisTemplate stringRedisTemplate) {
		List<Worker> workers = new ArrayList<>();
		for (int i = 0; i < cnt; i++)
			workers.add(new Worker(i, stringRedisTemplate));
		return workers;
	}

	private void log(int key, int id, String s) {
		System.out.println(key + ":" + id + " " + s);
	}
}
