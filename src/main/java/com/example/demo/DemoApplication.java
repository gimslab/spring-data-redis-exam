package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class DemoApplication {

	private static final String ACTION_SET = "set";
	private static final String KEY_MY_KEY = "a";
	private static final String DIR = "/tmp/redis-test";

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HHmmss");

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

		raceTest2();

//		redisTest_race(action());
//		redisTest_lock(action());
//		redisTest_string(action());
//		redisTest_keyval(action());
	}

	private void raceTest2() {
		File[] files = new File("/tmp/lock.test").listFiles();
		for (File file : files)
			file.delete();
		List<Worker> workers = Worker.newWorkers(3);
		workers.forEach(w -> w.start());
	}

	private void redisTest_race(String nodeId) {
		waitToZeroNano();
		for (int i = 0; i < 1000; i++) {
			String key = now().format(FMT);
			log("%s %s", nodeId, key);
			Boolean locked = getLockFor(key);
			if (locked) {
				log("SUCCEED locking - %s %s", nodeId, key);
				try {
					doJob(nodeId, key);
					log("job finished - %s %s", nodeId, key);
				} finally {
					releaseLockFor(nodeId, key);
					log("released lock - %s %s", nodeId, key);
				}
			} else {
				log("FAIL lock - %s %s", nodeId, key);
			}
			sleepms(100);
		}
	}

	private void waitToZeroNano() {
		while (now().getNano() != 0)
			sleepms(1L);
	}

	private void doJob(String nodeId, String key) {
		new File(DIR).mkdir();
		try {
			if (fileAlreadyExists(key)) {
				log("file already exists %s %s", nodeId, key);
				return;
			}
			new File(DIR, key + "-" + nodeId).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean fileAlreadyExists(String key) {
		String[] filenames = new File(DIR).list();
		for (String filename : filenames) {
			if (filename.startsWith(key))
				return true;
		}
		return false;
	}

	private Boolean getLockFor(String key) {
		BoundValueOperations<String, String> bvop = stringRedisTemplate.boundValueOps(redisKey(key));
		return bvop.setIfAbsent("1", 10, SECONDS);
	}

	private void releaseLockFor(String nodeId, String key) {
		BoundValueOperations<String, String> bvop = stringRedisTemplate.boundValueOps(redisKey(key));
		bvop.expire(0, SECONDS);
	}

	private String redisKey(String key) {
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

	private void sleep(long i) {
		sleepms(i * 1000);
	}

	private void sleepms(long i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

