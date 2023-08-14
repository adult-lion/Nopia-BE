package com.adultlion.nopia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class WebSocketSessionManagerConfig {
    private final RedisTemplate<String, Object> redisTemplate;

    public WebSocketSessionManagerConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public WebSocketSessionManager webSocketSessionManager() {
        return new WebSocketSessionManager(redisTemplate);
    }
}
