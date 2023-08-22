package com.adultlion.nopia.service;

import com.adultlion.nopia.component.ChatProperty;
import com.adultlion.nopia.component.ChatRoomScheduler;
import com.adultlion.nopia.component.ChatTopicProperty;
import com.adultlion.nopia.dto.ChatRoom;
import com.adultlion.nopia.dto.RequestPacket;
import com.adultlion.nopia.dto.ResponsePacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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

    // 의존성 주입받는 인스턴스
    private final ChatRoomScheduler scheduler; // 채팅방 투표관련 시간을 관리할 스케줄러
    private final ChatProperty chatProperty; // 채팅 프로퍼티를 가져온 클래스
    private final ChatTopicProperty chatTopicProperty; // 토픽 프로퍼티를 가져온 클래스
    private final ObjectMapper mapper; // 클라이언트에게 메시지를 보낼 때 문자열로 변환시키 위한 클래스

    // 대기방 및 채팅방 관련
    private ArrayList<LinkedList<WebSocketSession>> waitingUsers; // 대기유저가 LinkedList인 이유는 FIFO 순서성을 가져야 하기 때문. ArrayList에서 첫 번째는 주니어 대기방, 두 번째는 시니어 대기방
    private Map<String, ChatRoom> chatRooms; // 실제 유저들이 대화를 나눌 채팅방

    @PostConstruct // 클래스 내에서 의존성 주입을 마치면 자동으로 아래 메서드가 실행됨
    public void init() {
        // 대기방 리스트 초기화
        waitingUsers = new ArrayList<>();
        for (int i = 0; i < 2; i++) // 주니어 대기방, 시니어 대기방 생성
            waitingUsers.add(new LinkedList<>());

        // 채팅방 목록 초기화
        chatRooms = new HashMap<>();
    }

    // 사용자 대기방 입장
    /*
        · 사용자를 대기방으로 입장시키기 전 먼저 주니어, 시니어 대기방의 유저들 중 소켓 연결이 끊긴(세션을 종료한) 유저들을 제거.
        · 그리고 현재 세션을 주니어, 시니어 방 중 맞는 방에 추가함.
        · 주니어, 시니어 대기방의 인원이 새로운 채팅방을 만들기 위한 인원보다 같거나 많은 경우 새로운 채팅방을 생성함.
        · 채팅방을 생성한 후 대기유저 리스트를 pop하여 채팅방에 참여하라는 패킷을 전달.
        · 결과적으로, 만약 현재 대기 유저가 총 4명이고 한 방에 4명씩 매칭된다면 새로운 유저가 대기방으로 들어오면 앞의 대기유저 4명이 매칭되고 방금 들어온 유저는 대기방에서 다른 유저가 들어올 때까지 대기함.
     */
    public void join(WebSocketSession session, RequestPacket requestPacket) {
        waitingUsers.get(0).removeIf(sess -> !sess.isOpen()); // 주니어 대기유저에서 연결이 끊긴 모든 세션 삭제
        waitingUsers.get(1).removeIf(sess -> !sess.isOpen()); // 시니어 대기유저에서 연결이 끊긴 모든 세션 삭제

        waitingUsers.get(Integer.parseInt(requestPacket.getMessage())).add(session); // 현재 세션 대기방에 추가

        // 주니어와 시니어 모두 한 그룹에 들어갈 수 있는 인원보다 같거나 많은 경우
        if (waitingUsers.get(0).size() >= chatProperty.getJuniorInEachRoom() &&
                waitingUsers.get(1).size() >= chatProperty.getSeniorInEachRoom()) {
            String chatRoomId = UUID.randomUUID().toString(); // 새로운 채팅방 id 생성 (해당 id를 가지고 클라이언트에서 채팅과 함께 보내면 서버에서 id에 일치하는 채팅방을 연결해줌)

            ChatRoom room = new ChatRoom(scheduler, chatTopicProperty, chatRoomId); // 새로운 채팅방 생성

            // 생성된 채팅방의 ID를 포함한 응답패킷 생성하여 생성한 채팅방으로 입장 요청
            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.ENTER) // 클라이언트에게 채팅방으로 입장 요청
                    .roomId(chatRoomId).build();

            // 주니어 대기유저에게 패킷 전달 (패킷을 전달함과 동시에 대기 유저 pop)
            for (int tmp = 0; tmp < chatProperty.getJuniorInEachRoom(); tmp++)
                sendMessage(waitingUsers.get(0).pop(), message);
            // 시니어 대기유저에게 패킷 전달 (패킷을 전달함과 동시에 대기 유저 pop)
            for (int tmp = 0; tmp < chatProperty.getSeniorInEachRoom(); tmp++)
                sendMessage(waitingUsers.get(1).pop(), message);

            chatRooms.put(chatRoomId, room); // 생성한 채팅방 추가
        }
    }

    // 사용자 채팅방 입장
    // 채팅방 id를 통해 만약 해당하는 채팅방이 있는 경우 if문 하위 코드 실행
    public void enter(WebSocketSession session, RequestPacket requestPacket) {
        String roomId = requestPacket.getRoomId(); // 채팅방 ID
        if (chatRooms.containsKey(roomId)) { // 채팅방 ID가 존재하는 경우 해당 사용자 세션을 채팅방에 추가
            chatRooms.get(roomId).addSession(session, chatProperty.getTotalNumberOfUserInEachRoom(),requestPacket);
        }
    }

    // 사용자 간 대화
    // 채팅방 id를 통해 만약 해당하는 채팅방이 있는 경우 if문 하위 코드 실행
    public void talk(RequestPacket requestPacket) {
        String roomId = requestPacket.getRoomId(); // 채팅방 ID

        if (chatRooms.containsKey(roomId)) { // 채팅방 ID가 존재하는 경우 메시지 전달
            chatRooms.get(roomId).talk(requestPacket);
        }
    }

    // 사용자 투표 데이터 수집
    // 채팅방 id를 통해 만약 해당하는 채팅방이 있는 경우 if문 하위 코드 실행
    public void vote(RequestPacket requestPacket) {
        String roomId = requestPacket.getRoomId(); // 채팅방 ID

        if (chatRooms.containsKey(roomId)) { // 채팅방 ID가 존재하는 경우 메시지 전달
            chatRooms.get(roomId).vote(requestPacket);
        }
    }

    // 세션 끊김 방지
    // 클라이언트 쪽에서 PING을 보내면 서버에서 PONG을 보냄
    public void pingpong(WebSocketSession session, RequestPacket requestPacket) {
        ResponsePacket message = ResponsePacket.builder()
                .type(ResponsePacket.MessageType.PONG).build();

        sendMessage(session, message);
    }

    // 1분 마다 실행되는 세션 체크 함수 (1000ms = 1s)
    // 1분 마다 전체 채팅방을 순회하며 해당 채팅방의 인원 중 한명의 인원이라도 세션을 종료했다면 게임을 무효로 판단하고 해당 게임방 종료 및 제거
    @Scheduled(fixedDelay = 60000)
    public void sessionCheck(){
        chatRooms.entrySet().removeIf(entry -> !entry.getValue().checkAllSessionOpened());
    }

    // 대기방 내의 특정 세션에게 메시지 전달
    public <T> void sendMessage(WebSocketSession session, T message) {
        System.out.println(session.getId() + " <- " + message);
        try {
            if (session.isOpen()) // 세션이 열려 있을 경우에만 메시지를 전송함
                session.sendMessage(new TextMessage(mapper.writeValueAsString(message))); // 메시지 내용을 문자열로 변환하여 메시지를 전송함
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}