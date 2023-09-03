package com.example.websockets;

import com.example.redis.Publisher;
import com.example.redis.Subscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@Log4j2
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    WebSocketSessionManager webSocketSessionManager;

    @Autowired
    Publisher redisPublisher;

    @Autowired
    Subscriber redisSubscriber;

    // 내 local host로 요청이 왔을 때
    // http://localhost:8080/user/{userId}  => 이렇게 오는 요청들에 대해 적용
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketTextHandler(this.webSocketSessionManager, this.redisPublisher, this.redisSubscriber), "/user/*").
                addInterceptors(getParametersInterceptors()).  // 인터셉터 적용
                setAllowedOrigins("*");
    }

    @Bean
    public HandshakeInterceptor getParametersInterceptors() {
        return new HandshakeInterceptor() {
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {

                String path = request.getURI().getPath();
                String userId = WebSocketHelper.getUserIdFromUrl(path);  // abcd와 같은 유저 아이디 분리
                attributes.put(WebSocketHelper.userIdKey, userId);
                return true;
            }

            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
                // Nothing to do after handshake
            }
        };
    }
}
