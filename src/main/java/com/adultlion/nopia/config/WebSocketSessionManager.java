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

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, WebSocketSession session) {
        // Implement code to save session to Redis
        SessionInfo sessionInfo = new SessionInfo(session);
        ValueOperations<String,Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(sessionId, sessionInfo,30, TimeUnit.MINUTES);  // session을 30분동안 유지
    }

    public SessionInfo getSession(String sessionId) {
        System.out.println("getSession 호출됨"+sessionId);
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Object sessionObject = valueOps.get(sessionId);

        if (sessionObject instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> sessionMap = (LinkedHashMap<String, Object>) sessionObject;
            WebSocketSession webSocketSession = (WebSocketSession) sessionMap.get("session_id"); // 세션 정보의 키에 따라 수정
            return new SessionInfo(webSocketSession);
        }
        return null;
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
