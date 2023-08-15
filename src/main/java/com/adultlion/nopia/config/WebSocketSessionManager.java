package com.adultlion.nopia.config;

import com.adultlion.nopia.dto.SessionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;


public class WebSocketSessionManager {

    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    private ValueOperations<String, Object> valueOps;
    private SessionInfo sessionInfo;

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
        this.sessionInfo = new SessionInfo();
    }

    public void saveSession(String sessionId, WebSocketSession session) {
        // Implement code to save session to Redis
        sessionInfo.setId(sessionId);
        valueOps = redisTemplate.opsForValue();
        valueOps.set(sessionId, sessionInfo,30, TimeUnit.MINUTES);  // session을 30분동안 유지
    }

    public Object getSession(String sessionId) {
        Object sessionObject = valueOps ==null?null:valueOps.get(sessionId);
        if (sessionObject instanceof LinkedHashMap) {
            return valueOps.get(sessionId);
        }
        return sessionObject;
    }

    public void updateSession(String sessionId, WebSocketSession session) {
        // Implement code to update session in Redis
    }

    public void removeSession(String sessionId) {
        // Implement code to remove session from Redis
        redisTemplate.delete(sessionId);
    }

    public Boolean isSessionExist(String sessionId){
        return redisTemplate.hasKey(sessionId);
    }
}
