package com.adultlion.nopia;

import com.adultlion.nopia.handler.WebSocketChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SpringConfig implements WebSocketConfigurer {
    private final WebSocketChatHandler webSocketChatHandler;

    // 웹소켓 핸들러를 스프링에 등록
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 사용자가 요청한 주소가 `/ws` 하위 주소를 갖는 경우 아래 핸들러로 연결함
        registry.addHandler(webSocketChatHandler, "/ws").setAllowedOrigins("*");
    }
}