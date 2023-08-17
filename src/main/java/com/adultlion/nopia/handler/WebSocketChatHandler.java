package com.adultlion.nopia.handler;

import com.adultlion.nopia.dto.RequestPacket;
import com.adultlion.nopia.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final ChatService service;

    /*
        · 모든 소켓 메시지는 아래의 메서드를 통해 들어옴.
        · 패킷 내의 `type` 변수에 따라 여러 기능들로 분기함.
        · 핸들러를 이용해 특정 이벤트가 발생했을 때 실행되어야하는 메서드를 정의할 수 있음.
        · 소켓 통신을 할 때 핸들러를 이용해 데이터를 주고받는 이유는 스켓 통신은 언제든지 패킷을 주고 받아야 하기 때문.
        · 기존 컨트롤러는 사용자의 요청이 있을 때만 서버에서 데이터를 처리해 사용자에게 전달할 수 있음. (서버가 스스로 사용자에게 데이터를 전달할 수 없음)
        · 그래서 핸들러를 사용하여 메시지가 들어오면 이벤트를 발생시켜 아래 메서드를 실행시키도록 하고, 메시지를 사용자에게 보내야 하는 경우에도 핸들러의 이벤트를 발생시켜 사용자에게 메시지를 전달할 수 있도록 함.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // mapper 객체를 이용하여 들어온 패킷을 RequestPacket 클래스로 변환
        RequestPacket requestPacket = mapper.readValue(message.getPayload(), RequestPacket.class);

        System.out.println(requestPacket);

        // 패킷의 타입에 따라 서비스의 여러 기능들로 분기
        if (requestPacket.getType() == RequestPacket.MessageType.JOIN) { // 사용자 대기방 참여
            service.join(session, requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.ENTER) { // 사용자 채팅방 참여
            service.enter(session, requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.TALK) { // 사용자 간 대화
            service.talk(requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.VOTE) { // 사용자 투표 데이터
            service.vote(requestPacket);
        } else if (requestPacket.getType() == RequestPacket.MessageType.PING) { // 클라이언트 PING 데이터
            service.pingpong(session, requestPacket);
        }
    }

    // 클라이언트와 서버 간 세션이 종료되면 실행됨 (만약 클라이언트가 브라우저를 종료하거나 탭을 닫거나 세션을 종료하는 경우)
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
//        service.onSessionClosed(session);
    }
}