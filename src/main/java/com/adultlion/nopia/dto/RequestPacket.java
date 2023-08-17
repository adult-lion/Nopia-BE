package com.adultlion.nopia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestPacket {
    public enum MessageType { JOIN, ENTER, TALK, VOTE, PING }
    // JOIN - 대기방 입장 요청
    // ENTER - 채팅방 입장 요청
    // TALK - 대화 데이터
    // VOTE - 투표 데이터
    // PING - 세션 끊김 방지용(클라이언트)

    private MessageType type; // 메시지 타입
    private String roomId;    // 방 번호
    private String senderId;  // 채팅을 보낸 사용자의 세션 id
    private String senderNickname; // 채팅을 보낸 사용자의 닉네임
    private String message;   // 메시지 내용
    private String time;      // 채팅 발송 시간
}