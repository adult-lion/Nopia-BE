package com.adultlion.nopia.component;

import com.adultlion.nopia.dto.ChatTopic;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


/*
    · 해당 프로퍼티 클래스는 `/resources/application.yml`에 저장된 데이터를 가져오기 위한 클래스.
    · `@ConfigurationProperties("topic")` 어노테이션을 통해 `application.yml`에 접근하여 `topic` 내부의 데이터를 가져올 수 있음.
 */
@Component
@Getter
@Setter
@ConfigurationProperties("topic")
public class ChatTopicProperty {
    private int numberOfTopics; // 총 주제 수

    private List<ChatTopic> topics; // 주제들

    // topicId에 해당하는 ChatTopic 클래스를 반환
    public ChatTopic getTopic(int topicId) {
        return topics.get(topicId);
    }
}