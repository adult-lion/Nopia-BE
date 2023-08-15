package com.adultlion.nopia.component;

import com.adultlion.nopia.dto.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;

@Component
@EnableScheduling
public class ChatRoomScheduler { // 1초마다 mainRunner가 돌아가며 필요할 때마다 채팅방에 이벤트 발생
    private ArrayList<LinkedList<ChatRoom>> rooms; // 채팅방 배열, 2차원 구조인 이유는 같은 시간에 이벤트를 발생해야 하는 방이 여러개 일 수 있기 때문.

    private int currentIndex = 0; // 현재 스케줄러의 인덱스
    private int maxNumberOfRooms; // 채팅방의 최대 수 (타이머가 최대 기다릴 수 있는 시간)


    @Autowired
    private void init(ChatProperty chatProperty) { // 클래스 생성 시 실행되어야 하는 초기화 과정
        maxNumberOfRooms = chatProperty.getMaxNumberOfRooms();

        rooms = new ArrayList<>();
        for (int tmp = 0; tmp < maxNumberOfRooms; tmp++)
            rooms.add(new LinkedList<>());
    }

    public void addSchedule(ChatRoom room, int delay) { // 새로운 채팅방 추가
        // 만약 기다려야하는 시간이 최대로 기다릴 수 있는 시간(maxNumberOfRooms)보다 큰 경우 추가하지 않음
        if (delay > maxNumberOfRooms)
            return;

        rooms.get((currentIndex + delay) % maxNumberOfRooms).add(room);
    }

    /*
        · `Scheduled(fixedRate = 1000)`는 1초마다 아래 메서드를 실행하겠다는 의미.
        · 1초다마 `currentIndex`가 1씩 증가하며, `currentIndex`로 채팅방 배열(rooms)에 접근.
     */
    @Scheduled(fixedRate = 1000)
    private void mainRunner() { // 1초마다 반복되는 메인 함수
        if (!rooms.get(currentIndex).isEmpty()) { // 만약 현재 인덱스에 실행되어야 하는 이벤트가 있는 경우
            // 같은 시간에 여러 방의 이벤트가 발생해야할 수 있으므로 for문으로 전체 실행
            for (ChatRoom room : rooms.get(currentIndex)) {
                room.schedulerRunner(); // 해당 채팅방에 이벤트 발생
            }
            rooms.get(currentIndex).clear(); // 이벤트를 발생시킨 후에는 해당 인덱스에 대한 방들의 정보를 모두 삭제
        }

        // 만약 인덱스가 채팅방 배열의 인덱스보다 커지는 경우 다시 0으로 초기화, 그렇지 않으면 인덱스 1 증가
        currentIndex = currentIndex >= maxNumberOfRooms ? 0 : currentIndex + 1;
    }
}
