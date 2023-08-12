package com.adultlion.nopia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestPacket {
    public enum MessageType { JOIN, ENTER, TALK }
    // JOIN - 대기방 입장
    // ENTER - 채팅방 입장
    // TALK - 대화

    private MessageType type; // 메시지 타입
    private String roomId;    // 방 번호
    private int topicId;   // 토픽 ID
    private String sender;    // 채팅을 보낸 사람
    private String message;   // 메시지 내용
    private String time;      // 채팅 발송 시간
}