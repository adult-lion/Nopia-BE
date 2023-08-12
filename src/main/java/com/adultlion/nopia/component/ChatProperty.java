package com.adultlion.nopia.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("chat")
public class ChatProperty {
    private int numberOfAges; // 총 나이대 수
    private int juniorInEachRoom; // 각 방의 주니어 수
    private int seniorInEachRoom; // 각 방의 시니어 수
}
