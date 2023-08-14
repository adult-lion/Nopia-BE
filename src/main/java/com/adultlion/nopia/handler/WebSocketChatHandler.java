package com.adultlion.nopia.handler;

import com.adultlion.nopia.config.WebSocketSessionManager;
import com.adultlion.nopia.dto.RequestPacket;
import com.adultlion.nopia.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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

        // 패킷의 타입에 따라 서비스의 여러 기능들로 분기
        if (requestPacket.getType() == RequestPacket.MessageType.JOIN) {
            // 사용자 대기방 참여
            service.join(session, requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.ENTER) {
            // 사용자 채팅방 참여
            service.enter(session, requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.TALK) {
            // 사용자 간 대화
            service.talk(requestPacket);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        System.out.println("저장할 세션: " +session.getId());
        sessionManager.saveSession(session.getId(),session);
        // 세션이 종료되면 실행
        System.out.println("after에 들어온 세션 매니저: "+ sessionManager.getSession(session.getId()));
      //  sessionManager.saveSession(session.getId(), session);
    }
}