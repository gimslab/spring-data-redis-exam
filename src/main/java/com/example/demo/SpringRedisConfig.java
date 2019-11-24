package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class SpringRedisConfig {
	@Bean
	public LettuceConnectionFactory connectionFactory() {
//		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//		config.setHostName(hostName);
//		return new JedisConnectionFactory(config);
//		String hostName = "redis-test-1.cawzyo.ng.0001.usw2.cache.amazonaws.com";
		RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
		String hostName = "localhost";
		cfg.setHostName(hostName);
		return new LettuceConnectionFactory(cfg);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(){
		return new StringRedisTemplate(connectionFactory());
	}
}
