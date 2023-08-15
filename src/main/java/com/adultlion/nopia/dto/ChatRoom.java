package com.adultlion.nopia.dto;

import com.adultlion.nopia.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ChatRoom {
    private String roomId; // 채팅방 아이디
    private int topicId; // 토픽 ID
    private Set<WebSocketSession> sessions = new HashSet<>(); // 채팅방 내 사용자

    private final ObjectMapper mapper = new ObjectMapper();

    @Builder
    public ChatRoom(String roomId, int topicId) {
        this.roomId = roomId;
        this.topicId = topicId;
    }

    // 채팅방에 세션 추가
    public void addSession(WebSocketSession session, ChatService service, String topic) {
        sessions.add(session);

        // 세션 추가와 함께 현재 세션의 유저에게 채팅방 ID, 토픽 ID 등 여러 데이터 전달
        ResponsePacket message = ResponsePacket.builder()
                .type(ResponsePacket.MessageType.DATA)
                .roomId(this.roomId)
                .topicId(this.topicId)
                .message("주제 - '" + topic + "'")
                .sender(session.getId())
                .build();
        service.sendMessage(session, message);
    }

    // 채팅방 내의 모든 사용자에게 메시지 전송
    public <T> void sendMessage(T message, ChatService service) {
        sessions.parallelStream().forEach(session -> service.sendMessage(session, message));
    }
}