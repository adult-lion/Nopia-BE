package com.adultlion.nopia.handler;

import com.adultlion.nopia.config.WebSocketSessionManager;
import com.adultlion.nopia.dto.RequestPacket;
import com.adultlion.nopia.dto.SessionInfo;
import com.adultlion.nopia.service.ChatService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.Serializable;

@Component
@RequiredArgsConstructor

public class WebSocketChatHandler extends TextWebSocketHandler implements Serializable {
    private final ObjectMapper mapper;
    private final ChatService service;
    private final WebSocketSessionManager sessionManager;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // mapper 객체를 이용하여 들어온 패킷을 RequestPacket 클래스로 캐스팅
        RequestPacket requestPacket = mapper.readValue(message.getPayload(), RequestPacket.class);

        // 유저가 새로고침 버튼을 누른 경우
        if("refresh".equals(requestPacket.getMessage())){
            System.out.println("Message.getPayload: "+message.getPayload());
            if(isRefreshNeeded(session)){
                System.out.println("새로고침 감지 세션 id: "+session.getId());
                restoreSession(session);
            }
        }
        else {
            // 패킷의 타입에 따라 서비스의 여러 기능들로 분기
            if (requestPacket.getType() == RequestPacket.MessageType.JOIN) {
                // 사용자 대기방 참여
                service.join(session, requestPacket);
            } else if (requestPacket.getType() == RequestPacket.MessageType.ENTER) {
                // 사용자 채팅방 참여
                service.enter(session, requestPacket);

            } else if (requestPacket.getType() == RequestPacket.MessageType.TALK) {
                // 사용자 세션 저장
                if(sessionManager.getSession(requestPacket.getSender()) == null){
                    sessionManager.saveSession(requestPacket.getSender(),session);
                }
                // 사용자 간 대화
                service.talk(requestPacket);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {}

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
//        System.out.println("afterConnectionClosed: "  +session.getId());
//       // sessionManager.saveSession(session.getId(),session);
//        // 세션이 종료되면 실행
//        System.out.println("after에 들어온 세션 매니저: "+ sessionManager.getSession(session.getId()).getId());
    }

    // 새로고침이 필요한지 여부를 판단하는 메서드
    private boolean isRefreshNeeded(WebSocketSession session){
        return sessionManager.isSessionExist(session.getId());
    }

    // 세션 복원 로직 수행 메서드
    private void restoreSession(WebSocketSession session){
        // Redis에서 세션 정보를 가져와 WebSocket 연결 재설정
        Object savedSession = sessionManager.getSession(session.getId());   // 기존 세션 가져오기
        if(savedSession!=null){
                String message = "Welcome back! Your session has been restored.";
                System.out.println(message);
        }
    }
}