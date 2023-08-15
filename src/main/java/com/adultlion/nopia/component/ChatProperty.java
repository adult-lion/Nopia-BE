package com.adultlion.nopia.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/*
    · 해당 프로퍼티 클래스는 `/resources/application.yml`에 저장된 데이터를 가져오기 위한 클래스.
    · `@ConfigurationProperties("chat")` 어노테이션을 통해 `application.yml`에 접근하여 `chat` 내부의 데이터를 가져올 수 있음.
 */
@Component
@Getter
@Setter
@ConfigurationProperties("chat")
public class ChatProperty {
    private int maxNumberOfRooms; // 최대 생성 가능한 방의 수
    private int numberOfAges; // 총 나이대 수
    private int juniorInEachRoom; // 각 방의 주니어 수
    private int seniorInEachRoom; // 각 방의 시니어 수
    private int secondEachGamePhase; // 게임 단계 별 제한시간

    // 각 방의 최다 인원을 반환
    public int getTotalNumberOfUserInEachRoom() { return juniorInEachRoom + seniorInEachRoom; }
}
