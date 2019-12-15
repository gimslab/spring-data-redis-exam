package com.gimslab.redisexam.simpledistributedlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.File;
import java.util.List;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class SimpleDistributedLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleDistributedLockApplication.class, args);
	}

	private StringRedisTemplate stringRedisTemplate;

	public SimpleDistributedLockApplication(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
		raceTest2();
	}

	private void raceTest2() {
		prepareTmpDir();
		List<Worker> workers = Worker.newWorkers(10, stringRedisTemplate);
		workers.forEach(w -> w.start());
	}

	private void prepareTmpDir() {
		File dir = new File("/tmp/lock.test");
		if (!(dir.exists())) {
			dir.mkdir();
			return;
		}
		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (File file : files)
			file.delete();
	}
}

