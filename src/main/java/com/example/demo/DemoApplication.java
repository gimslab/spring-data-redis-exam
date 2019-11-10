package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

@SpringBootApplication
@Import(SpringRedisConfig.class)
public class DemoApplication {

	private static final String ACTION_SET = "set";
	private static final String KEY_MY_KEY = "myKey";

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
		System.out.println("action = " + actionArg());

//		redisTest_keyval(actionArg());
		redisTest_string(actionArg());
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

	private String actionArg() {
		List<String> args = applicationArguments.getNonOptionArgs();
		return args.size() > 0 ? args.get(0) : null;
	}
}

