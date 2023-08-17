package com.adultlion.nopia.dto;

import com.adultlion.nopia.component.ChatRoomScheduler;
import com.adultlion.nopia.component.ChatTopicProperty;
import com.adultlion.nopia.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Slf4j
@Data
@RequiredArgsConstructor
public class ChatRoom {

    // 생성자에 포함됨
    private final ChatRoomScheduler scheduler; // 방 제한시간을 관리할 스케줄러
    private final ChatTopicProperty chatTopicProperty; // 토픽 프로퍼티를 가져온 클래스

    // 멤버 변수
    private final String id; // 채팅방 아이디
    private int topicId = 0; // 토픽 ID
    private int gamePhase = 0; // 0 - 사용자 입장, 1 - 첫 번째 날, 2 - 두 번째 날 ...
    private Map<String, Integer> votes = new HashMap<>(); // 투표 집계가 될 변수
    private int votesCounter = 0; // 현재 집계된 투표 수

    // 채팅방 내의 유저 데이터를 담당
    private Map<String, WebSocketSession> sessions = new LinkedHashMap<>(); // 채팅방 내의 사용자 세션
    private Map<String, String> nicknames = new LinkedHashMap<>(); // 채팅방 내의 사용자 닉네임

    // 유틸리티
    private final Random random = new Random(); // 랜덤 주제를 뽑기위한 랜덤 클래스
    private final ObjectMapper mapper = new ObjectMapper(); // 사용자에게 메시지를 전달할 때 메시지 내용을 문자열로 변환시키기 위한 클래스


    // 채팅방에 세션 추가
    /*
        · 세션이 추가되면 sessions, nicknames에 각각 세션의 정보와 닉네임 데이터가 저장됨.
        · 그 후 추가된 세션에게 생성된 세션 id와 닉네임을 반환하여 클라이언트 단에서 저장할 수 있도록 함.
        · 사용자가 추가된 후 방 최대 인원(totalNumberOfUser)과 현재 들어온 사용자 수가 같으면 게임 시작.
     */
    public void addSession(WebSocketSession session, int totalNumberOfUser, RequestPacket requestPacket) {
        String sessionId = requestPacket.getSenderId(); // 새로고침한 사용자일 경우 senderId를 전송함

        if (!sessions.containsKey(sessionId)) { // 처음 접속한 사용자인 경우
            sessionId = session.getId();

            sessions.put(sessionId, session);
            nicknames.put(sessionId, "익명" + sessions.size());

            // 세션 추가와 함께 현재 세션의 사용자에게 채팅방 ID 데이터 전달
            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.NOTICE)
                    .senderId(sessionId) // 사용자의 세션 id와 닉네임을 반환하여 사용자가 저장할 수 있도록 함
                    .senderNickname(nicknames.get(sessionId))
                    .message(nicknames.get(sessionId) + "님이 채팅방에 입장하였습니다.")
                    .build();
            sendMessage(session, message);

            // 만약 모든 사용자가 들어오면 게임 시작
            if (sessions.size() == totalNumberOfUser)
                startGame();
        } else { // 재접속한 사용자인 경우
            // 신규 세션 추가
            sessions.put(session.getId(), session);
            nicknames.put(session.getId(), nicknames.get(sessionId));

            // 기존 세션 삭제
            sessions.remove(sessionId);
            nicknames.remove(sessionId);

            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.NOTICE)
                    .senderId(session.getId()) // 사용자의 세션 id와 닉네임을 반환하여 사용자가 저장할 수 있도록 함
                    .senderNickname(nicknames.get(session.getId()))
                    .message(nicknames.get(session.getId()) + "님이 채팅방에 재입장하였습니다.")
                    .build();
            sendMessage(session, message);
        }
    }

    // 채팅방 게임 시작
    /*
        · 게임을 시작하기 전 랜덤으로 주제 하나를 선택함
        · 클라이언트에 현재 채팅방 내에 있는 사용자들의 정보(세션 id, 닉네임)을 전달하여 저장하도록 함 (나중에 클라이언트가 투표를 진행할 때는 지목한 사용자의 세션 id를 전송함)
        · 게임이 시작됨을 알리는 동시에 주제 정보와 함께 NOTICE 패킷을 클라이언트로 전달함.
        · 시작된 후 `delay`초 이후 투표를 진행하도록 타이머를 시작시킴
     */
    public void startGame() {
        // 랜덤 주제 선정
        topicId = random.nextInt(chatTopicProperty.getNumberOfTopics());

        // 사용자에게 보낼 유저 리스트 생성
        ArrayList<ChatUser> users = new ArrayList<>();
        for (String key : nicknames.keySet())
            users.add(ChatUser.builder()
                    .id(key)
                    .nickname(nicknames.get(key)).build());

        // 모든 사용자에게 대화 주제 전달
        ResponsePacket message = ResponsePacket.builder()
                .type(ResponsePacket.MessageType.NOTICE)
                .message("주제 - " + chatTopicProperty.getTopic(topicId).getTopic())
                .aliveUsers(users.toArray(new ChatUser[0]))
                .build();
        broadcast(message);

        gamePhase = 1; // 첫 번째 날 시작
        scheduler.addSchedule(this, 10); // delay초 후 투표 진행 타이머 시작

    }

    // 다음 게임 스탭 시간이 되면 ChatRoomScheduler에서 실행됨
    /*
        · 타이머의 시간이 되면 첫 번째 날이 시작된지 먼저 확인한 후 투표 정보를 클라이언트에게 보냄.
        · 클라이언트에게 투표자 배열을 보내기 위해 유저 리스트를 생성함
        · 클라이언트에게 전송할 패킷은 사용자들의 배열을 담아야 하기 때문에 `users.toArray()`로 리스트를 배열로 변환하여 패킷 전송
     */
    public void schedulerRunner() {
        if (gamePhase == 1) {
            // 사용자에게 보낼 유저 리스트 생성
            ArrayList<ChatUser> users = new ArrayList<>();
            for (String key : nicknames.keySet())
                users.add(ChatUser.builder()
                        .id(key)
                        .nickname(nicknames.get(key)).build());

            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.VOTE)
                    .message("첫 번째 날 투표")
                    .aliveUsers(users.toArray(new ChatUser[0])).build();
            broadcast(message);
        }
    }

    // 사용자 간 대화
    /*
        · 메시지를 보낸 사용자의 패킷에서 메시지 내용, 보낸 사람 세션 id, 보낸 사람 닉네임의 정보를 그대로 다른 사람에게 전달함.
        · 이때 해당 메시지를 보낸 사람에게도 그대로 패킷을 반환함으로써 정상적으로 전달되었음을 알림.
     */
    public void talk(RequestPacket requestPacket) {
        // 메시지 전달은 보낸 유저와 받는 유저 모두에게 전달함
        // 보낸 유저에게 메시지를 다시 반환함으로써 정상적으로 전달됨을 전달
        ResponsePacket message = ResponsePacket.builder()
                .type(ResponsePacket.MessageType.TALK)
                .message(requestPacket.getMessage())
                .senderId(requestPacket.getSenderId())
                .senderNickname(requestPacket.getSenderNickname())
                .build();
        broadcast(message);
    }

    // 사용자 투표 데이터 수집
    /*
        · 클라이언트는 패킷 내의 message에 지목한 사용자의 세션 id를 전송함.
        · 그러면 해당 정보를 하나씩 모아 총 투표 수와 사용자 세션의 수가 같으면 집계 시작
        · 집계를 하기 위해 최다 득표 유저를 계산한 뒤 결과를 패킷에 담아 전달함
     */
    public void vote(RequestPacket requestPacket) {
        // 투표 데이터 저장
        int vote = votes.getOrDefault(requestPacket.getMessage(), 0); // 이전에 투표가 진행된 사용자라면 그대로 값을 가져오고, 그렇지 않으면(새로 지목된 사용자) 0의 값을 가져옴
        votes.put(requestPacket.getMessage(), vote + 1); // 데이터 변경
        votesCounter++;

        // 투표 집계 완료
        if (votesCounter >= nicknames.size()) {
            // 최다 득표 유저 계산
            String resultUserNickname = ""; // 최다 득표 유저의 닉네임이 저장됨
            int resultUserVote = 0; // 최다 득표 유저의 득표수가 저장됨
            for (String key : votes.keySet()) {
                if (votes.get(key) > resultUserVote) {
                    resultUserNickname = nicknames.get(key);
                    resultUserVote = votes.get(key);
                }
            }

            // 메시지 전달
            ResponsePacket message = ResponsePacket.builder()
                    .type(ResponsePacket.MessageType.RESULT)
                    .message(resultUserNickname + "님 " + resultUserVote + "표")
                    .build();
            broadcast(message);
        }
    }

    // 특정 한 세션에게 메시지 전달
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            if (session.isOpen()) // 세션이 열려 있을 경우에만 메시지를 전송함
                session.sendMessage(new TextMessage(mapper.writeValueAsString(message))); // 메시지를 문자열 형식으로 변환하여 메시지 전송
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // 채팅방 내의 모든 세션에게 메시지 전송
    public <T> void broadcast(T message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    // 채팅방의 모든 세션 종료
    public void closeSessions() {
        sessions.forEach((key, session) -> {
            try {
                if (session.isOpen()) // 세션이 열려있을 때만 세션 종료
                    session.close();
            } catch (IOException e) {
                log.error(e.toString());
            }
        });
    }
}