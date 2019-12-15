package com.gimslab.redisexam.simpledistributedlock;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RedisSimpleLock implements AutoReleasableLock {

	private static final String KEY_PREFIX = "redis.key.test-";
	private static final int TIMEOUT_SEC_60 = 60;

	private int key;
	private StringRedisTemplate stringRedisTemplate;

	private RedisSimpleLock(int key, StringRedisTemplate stringRedisTemplate) {
		this.key = key;
		this.stringRedisTemplate = stringRedisTemplate;
	}

	static AutoReleasableLock getLockForKey(int key, StringRedisTemplate stringRedisTemplate) throws LockFailedException {
		BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(redisKey(key));
		Boolean ok = ops.setIfAbsent("1", TIMEOUT_SEC_60, SECONDS);
		if (ok == null || !ok)
			throw new LockFailedException("cannot set key: " + redisKey(key));
		return new RedisSimpleLock(key, stringRedisTemplate);
	}

	@Override
	public void close() {
		BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(redisKey(key));
		ops.expire(0, SECONDS);
	}

	private static String redisKey(int key) {
		return KEY_PREFIX + ":" + key;
	}

	@Override
	public String toString() {
		return "RedisSimpleLock(key:" + key + ")";
	}
}
