package com.adultlion.nopia.service;

import com.adultlion.nopia.component.ChatProperty;
import com.adultlion.nopia.component.TopicProperty;
import com.adultlion.nopia.dto.ChatRoom;
import com.adultlion.nopia.dto.RequestPacket;
import com.adultlion.nopia.dto.ResponsePacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class ChatService {
    private final ChatProperty chatProperty; // 채팅 프로퍼티를 가져온 클래스
    private final TopicProperty topicProperty; // 토픽 프로퍼티를 가져온 클래스
    private final ObjectMapper mapper;

    private final Random random = new Random();

    private ArrayList<LinkedList<WebSocketSession>> waitingUsers; // 대기유저가 LinkedList인 이유는 FIFO 순서성을 가져야 하기 때문
    private Map<String, ChatRoom> chatRooms = new HashMap<>(); // 실제 유저들이 대화를 나눌 채팅방

    @PostConstruct // 클래스 내에서 의존성 주입을 마치면 실행
    public void init() {
        // 대기유저 리스트 초기화
        waitingUsers = new ArrayList<>();
        for (int i = 0; i < 2; i++) // 주니어, 시니어
            waitingUsers.add(new LinkedList<>());
    }

    // 사용자 대기방 입장
    public void join(WebSocketSession session, RequestPacket requestPacket) {
        // 세션이 끊긴 주니어 대기유저의 세션 삭제
        for (int i = 0; i < waitingUsers.get(0).size(); i++) {
            if (waitingUsers.get(0).get(0).isOpen())
                break;
            waitingUsers.get(0).pop();
        }
        // 세션이 끊긴 시니어 대기유저의 세션 삭제
        for (int i = 0; i < waitingUsers.get(1).size(); i++) {
            if (waitingUsers.get(1).get(0).isOpen())
                break;
            waitingUsers.get(1).pop();
        }

        waitingUsers.get(Integer.parseInt(requestPacket.getMessage())).add(session); // 현재 세션 대기방에 추가

        // 주니어와 시니어 모두 대기하는 사용자가 존재하는 경우
        if (waitingUsers.get(0).size() >= chatProperty.getJuniorInEachRoom() &&
                waitingUsers.get(1).size() >= chatProperty.getSeniorInEachRoom()) {
            String chatRoomId = UUID.randomUUID().toString();
            int topicId = random.nextInt(topicProperty.getNumberOfTopics() + 1);

            ChatRoom room = ChatRoom.builder()
                    .roomId(chatRoomId)
                    .topicId(topicId).build(); // 새로운 채팅방 생성

            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.DATA)
                    .roomId(chatRoomId)
                    .topicId(topicId)
                    .build(); // 생성된 채팅방의 ID를 포함한 응답패킷 생성

            // 주니어 대기유저에게 패킷 전달 (패킷을 전달함과 동시에 대기 유저 pop)
            for (int tmp = 0; tmp < chatProperty.getJuniorInEachRoom(); tmp++)
                sendMessage(waitingUsers.get(0).pop(), message);
            // 시니어 대기유저에게 패킷 전달 (패킷을 전달함과 동시에 대기 유저 pop)
            for (int tmp = 0; tmp < chatProperty.getSeniorInEachRoom(); tmp++)
                sendMessage(waitingUsers.get(1).pop(), message);

            chatRooms.put(chatRoomId, room); // 채팅방 생성
        }
    }

    // 사용자 채팅방 입장
    public void enter(WebSocketSession session, RequestPacket requestPacket) {
        int topicId = requestPacket.getTopicId(); // 토픽 ID
        String roomId = requestPacket.getRoomId(); // 채팅방 ID

        if (chatRooms.containsKey(roomId)) { // 토픽, 채팅방 ID가 모두 있는 경우 해당 사용자 세션을 채팅방에 추가
            chatRooms.get(roomId).addSession(session, this, topicProperty.getTopic(topicId).getTopic());
        }
    }

    // 사용자 간 대화
    public void talk(RequestPacket requestPacket) {
        int topicId = requestPacket.getTopicId(); // 토픽 ID
        String roomId = requestPacket.getRoomId(); // 채팅방 ID

        if (chatRooms.containsKey(roomId)) { // 토픽, 채팅방 ID가 모두 있는 경우 메시지 전달
            // 메시지 전달은 보낸 유저와 받는 유저 모두에게 전달함
            // 보낸 유저에게 메시지를 다시 반환함으로써 정상적으로 전달됨을 전달
            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.TALK)
                    .roomId(roomId)
                    .topicId(topicId)
                    .message(requestPacket.getMessage())
                    .sender(requestPacket.getSender())
                    .build();
            chatRooms.get(roomId).sendMessage(message, this);
        }
    }

    // 특정 세션에게 메시지 전달
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}