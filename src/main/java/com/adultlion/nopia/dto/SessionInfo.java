package com.adultlion.nopia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
@NoArgsConstructor
public class SessionInfo {
    private String id;

    public SessionInfo(WebSocketSession session){
        if(session==null) {
            System.out.println("시벌 NULL 값이요");
            return;
        }

        System.out.println("SessionInfo의 session: " + session);
        this.id = session.getId();
        }
}
