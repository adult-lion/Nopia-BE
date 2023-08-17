package com.adultlion.nopia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponsePacket {
    public enum MessageType { ENTER, NOTICE, TALK, VOTE, RESULT, PONG }
    // ENTER - 클라이언트에게 채팅방 입장 요청
    // NOTICE - 클라이언트에게 알림(공지)
    // TALK - 다른 사용자가 보낸 대화
    // VOTE - 클라이언트에게 투표 시작 알림
    // RESULT - 투표 결과
    // PONG - 세션 끊김 방지용(서버)


    private ResponsePacket.MessageType type; // 메시지 타입
    private String roomId;    // 방 ID
    private String senderId;  // 채팅을 보낸 사용자의 세션 id
    private String senderNickname; // 채팅을 보낸 사용자의 닉네임
    private String message;   // 메시지 내용
    private ChatUser[] aliveUsers; // 살아있는 유저 배열
}