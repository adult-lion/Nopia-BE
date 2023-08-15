package com.adultlion.nopia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatUser {
    private String id;           // 사용자 세션 id
    private String nickname;     // 사용자 닉네임
}
