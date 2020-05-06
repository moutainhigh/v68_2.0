/**
 * 
 */
package com.zyhy.common_server.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Administrator
 * 日志的Redis操作
 */
public class ClubRedisTemplate extends RedisTemplate<String, String>{

	public ClubRedisTemplate(){
		RedisSerializer<String> stringSerializer = new StringRedisSerializer();
		setKeySerializer(stringSerializer);
		setValueSerializer(stringSerializer);
		setHashKeySerializer(stringSerializer);
		setHashValueSerializer(stringSerializer);	}

	public ClubRedisTemplate(LettuceConnectionFactory connectionFactory) {
		this();
		connectionFactory.setDatabase(4);
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}
	
	protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
		return new DefaultStringRedisConnection(connection);
	}
}
