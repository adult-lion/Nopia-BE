package com.adultlion.nopia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@Getter
@NoArgsConstructor
public class SessionInfo {
    private String id;

    public SessionInfo(WebSocketSession session){
        if(session==null) {
            System.out.println("시벌 NULL 값이요");
            return;
        }

        System.out.println("SessionInfo 세션 아이디: " + session.getId());
        this.id = session.getId();
        }
}
