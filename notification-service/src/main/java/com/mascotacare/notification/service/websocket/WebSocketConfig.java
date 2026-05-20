package com.mascotacare.notification.service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/notifications")
                .addInterceptors(userIdInterceptor())
                .setAllowedOriginPatterns("*");
    }

    /**
     * El gateway ya validó el JWT y propagó X-User-Id como header en el handshake
     * (que sí es una request HTTP de upgrade, así que conserva los headers).
     * Lo movemos a session.attributes para que el handler lo lea por sesión.
     */
    private HandshakeInterceptor userIdInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attrs) {
                if (request instanceof ServletServerHttpRequest servletReq) {
                    String userId = servletReq.getServletRequest().getHeader("X-User-Id");
                    String role = servletReq.getServletRequest().getHeader("X-User-Role");
                    if (userId == null || userId.isBlank()) {
                        log.debug("WS handshake rechazado — falta X-User-Id");
                        return false;
                    }
                    try {
                        attrs.put("userId", UUID.fromString(userId));
                        attrs.put("role", role);
                        return true;
                    } catch (IllegalArgumentException e) {
                        log.debug("WS handshake con userId inválido: {}", userId);
                        return false;
                    }
                }
                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                       WebSocketHandler h, Exception ex) {}
        };
    }
}
