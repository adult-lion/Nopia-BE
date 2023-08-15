package com.adultlion.nopia.component;

import com.adultlion.nopia.dto.Topic;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties("topic")
public class TopicProperty {
    private int numberOfTopics;

    private List<Topic> topics;

    public Topic getTopic(int topicId) {
        return topics.get(topicId - 1);
    }
}