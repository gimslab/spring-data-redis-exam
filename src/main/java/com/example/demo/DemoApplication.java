package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private RedisTemplate<String, Object> redisTemplate;
	private ApplicationArguments applicationArguments;

	public DemoApplication(RedisTemplate<String, Object> redisTemplate, ApplicationArguments applicationArguments) {
		this.redisTemplate = redisTemplate;
		this.applicationArguments = applicationArguments;

		System.out.println("+++++++++++++++++++++");
		redisTest(actionArg());
		actionArg();
	}

	private String actionArg() {
		List<String> args = applicationArguments.getNonOptionArgs();
		return args.get(0);
	}

	private void redisTest(String action) {
		System.out.println("action = "+action);
		try {
			ValueOperations<String, Object> values = redisTemplate.opsForValue();

			if(action.equals("set"))
				values.set("myKey", "myVal");

			System.out.println("get result = " + values.get("myKey"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

