package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.core.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class DemoApplication {

	private static final String ACTION_SET = "set";
	private static final String KEY_MY_KEY = "a";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private RedisTemplate<String, Object> redisTemplate;
	private StringRedisTemplate stringRedisTemplate;
	private ApplicationArguments applicationArguments;

	public DemoApplication(RedisTemplate<String, Object> redisTemplate,
	                       StringRedisTemplate stringRedisTemplate, ApplicationArguments applicationArguments) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.applicationArguments = applicationArguments;

		System.out.println("+++++++++++++++++++++");
		System.out.println("action = " + action());

		redisTest_race(action());
//		redisTest_lock(action());
//		redisTest_string(action());
//		redisTest_keyval(action());
	}

	private void redisTest_race(String nodeId) {
		for (int i = 0; i < 1000; i++) {
			int key = LocalDateTime.now().getSecond();
			log("%s %s", nodeId, key);
			try {
				try {
					getLockFor(key);
					log("SUCCEED locking - %s %s", nodeId, key);
					doJob(nodeId, key);
					log("job finished - %s %s", nodeId, key);
				} finally {
					releaseLockFor(nodeId, key);
					log("released lock - %s %s", nodeId, key);

				}
			} catch (LockFail lockFail) {
				log("lock fail for key %s %s", nodeId, key);
			}
			sleep(3);
		}
	}

	private void doJob(String nodeId, int key) {
		String dir = "/tmp/redis-test";
		new File(dir).mkdir();
		try {
			new File(dir, key + "-" + nodeId).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getLockFor(int key) throws LockFail {
		BoundValueOperations<String, String> bvop = stringRedisTemplate.boundValueOps(redisKey(key));
		Boolean succeed = bvop.setIfAbsent("1", 10, SECONDS);
		if (!succeed)
			throw new LockFail(redisKey(key));
	}

	private void releaseLockFor(String nodeId, int key) {
		BoundValueOperations<String, String> bvop = stringRedisTemplate.boundValueOps(redisKey(key));
		Boolean succeed = bvop.expire(0, SECONDS);
		log("expired in 0 sec. %s %s", nodeId, key);
	}

	private String redisKey(int key) {
		return KEY_MY_KEY + ":" + key;
	}

	private class LockFail extends Throwable {
		private String redisKey;

		public LockFail(String redisKey) {
			this.redisKey = redisKey;
		}
	}

	private void redisTest_lock(String action) {
		ValueOperations<String, String> val = stringRedisTemplate.opsForValue();
		if (ACTION_SET.equals(action)) {
			RedisOperations<String, String> op = val.getOperations();
			BoundValueOperations<String, String> bv = op.boundValueOps(KEY_MY_KEY);
			for (int i = 0; i < 5; i++) {
				Boolean r = bv.setIfAbsent("bbbbb", 10, SECONDS);
				log("+++ setResult=%s,", r);
				if (r)
					break;
				log("+++ retry to set");
				sleep(3);
			}
		}
		log("get result = %s", val.get(KEY_MY_KEY));
	}

	private void redisTest_string(String action) {
		ValueOperations<String, String> val = stringRedisTemplate.opsForValue();
		if (ACTION_SET.equals(action)) {
			val.set(KEY_MY_KEY, "aaaaaaa");
			System.out.println("+++ value set");
		}
		System.out.println("get result = " + val.get(KEY_MY_KEY));
	}

	private void redisTest_keyval(String action) {
		ValueOperations<String, Object> values = redisTemplate.opsForValue();

		if (action.equals(ACTION_SET)) {
			values.set(KEY_MY_KEY, "myVal");
			System.out.println("+++ value set");
		}

		System.out.println("get result = " + values.get(KEY_MY_KEY));
	}

	private String action() {
		List<String> args = applicationArguments.getNonOptionArgs();
		return args.size() > 0 ? args.get(0) : null;
	}

	private void log(String format, Object... args) {
		System.out.printf("+++ " + format + lineSeparator(), args);
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

