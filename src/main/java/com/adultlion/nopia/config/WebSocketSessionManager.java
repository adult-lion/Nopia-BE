package com.adultlion.nopia.config;

import com.adultlion.nopia.dto.SessionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;


public class WebSocketSessionManager {

    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    private ValueOperations<String, Object> valueOps;

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, WebSocketSession session) {
        // Implement code to save session to Redis
        SessionInfo sessionInfo = new SessionInfo(session);
        valueOps = redisTemplate.opsForValue();
        System.out.println("saveSession: "+ sessionInfo);
        valueOps.set(sessionId, sessionInfo,30, TimeUnit.MINUTES);  // session을 30분동안 유지
    }

    public SessionInfo getSession(String sessionId) {
        Object sessionObject = valueOps ==null?null:valueOps.get(sessionId);
        System.out.println("sessionObject: "+sessionObject);

        if (sessionObject instanceof LinkedHashMap) {
            System.out.println("getSession의 LinkedHashMap 조건문: "+sessionObject);
            LinkedHashMap<String, Object> sessionMap = (LinkedHashMap<String, Object>) sessionObject;
            System.out.println(valueOps.get(sessionMap.values().iterator().next())); // 첫번째 값 가져오기);
            return (SessionInfo) valueOps.get(sessionMap.values().iterator().next());
        }
        return (SessionInfo) sessionObject;
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
