package com.auth.template.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private GenericJackson2JsonRedisSerializer customJackson2JsonRedisSerializer(ObjectMapper defaultObjectMapper) {

        // Tạo bản sao để không làm hỏng cấu hình API toàn cục của Spring
        ObjectMapper redisObjectMapper = defaultObjectMapper.copy();

        // Cấu hình riêng cho Redis: Lưu tên class vào JSON để Deserialize chính xác
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper defaultObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serialize Key dưới dạng String
        template.setKeySerializer(new StringRedisSerializer());

        // Serialize Value dưới dạng JSON (thay vì JdkSerialization mặc định)
        template.setValueSerializer(customJackson2JsonRedisSerializer(defaultObjectMapper));

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,  ObjectMapper defaultObjectMapper) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // Serialize Key thành String
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Serialize Value thành JSON
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(customJackson2JsonRedisSerializer(defaultObjectMapper)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
