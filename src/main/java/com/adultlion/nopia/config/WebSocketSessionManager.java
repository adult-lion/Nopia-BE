package com.adultlion.nopia.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.TimeUnit;

public class WebSocketSessionManager {
    private final RedisTemplate<String, Object> redisTemplate;

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, WebSocketSession session) {
        // Implement code to save session to Redis
        ValueOperations<String,Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(sessionId, session,30, TimeUnit.MINUTES);
    }

    public WebSocketSession getSession(String sessionId) {
        System.out.println("getSession 호출됨"+sessionId);
        ValueOperations<String,Object> valueOps = redisTemplate.opsForValue();
        return (WebSocketSession) valueOps.get(sessionId);

    }

    public void updateSession(String sessionId, WebSocketSession session) {
        // Implement code to update session in Redis
    }

    public void removeSession(String sessionId) {
        // Implement code to remove session from Redis
        redisTemplate.delete(sessionId);
    }
}
