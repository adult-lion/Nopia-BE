package com.adultlion.nopia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponsePacket {
    public enum MessageType { DATA, TALK }
    // DATA - 서버에서 데이터 전송
    // TALK - 대화


    private ResponsePacket.MessageType type; // 메시지 타입
    private String roomId;    // 방 ID
    private int topicId;   // 토픽 ID
    private String sender;    // 채팅을 보낸 사람
    private String message;   // 메시지 내용
    private String time;      // 채팅 발송 시간
}