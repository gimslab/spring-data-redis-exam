package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private RedisTemplate<String, Object> redisTemplate;

	public DemoApplication(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;

		System.out.println("+++++++++++++++++++++");
		redisTest();
	}

	private void redisTest() {

		try {
			ValueOperations<String, Object> values = redisTemplate.opsForValue();

			values.set("myKey", "myVal");

			System.out.println("get result = " + values.get("myKey"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

