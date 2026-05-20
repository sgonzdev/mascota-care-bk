package com.mascotacare.notification.service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mascotacare.notification.service.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

/**
 * Gestiona conexiones WebSocket activas. Cada sesión queda asociada al userId
 * (extraído del JWT propagado por el gateway en el handshake).
 *
 * Protocolo:
 *   - Cliente conecta a ws://gateway/ws?token=<JWT>.
 *   - Servidor empuja mensajes JSON con shape NotificationResponse cuando se crea
 *     una notificación PERSONAL (para ese user) o GLOBAL (para todos).
 *   - No hay mensajes cliente→servidor; el cliente solo recibe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /** userId → sesiones activas (un usuario puede tener varias pestañas abiertas). */
    private final Map<UUID, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = userIdOf(session);
        if (userId == null) {
            silentClose(session, CloseStatus.POLICY_VIOLATION.withReason("Falta userId"));
            return;
        }
        sessionsByUser.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("WS abierto userId={} sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = userIdOf(session);
        if (userId == null) return;
        var sessions = sessionsByUser.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) sessionsByUser.remove(userId);
        }
        log.debug("WS cerrado userId={} reason={}", userId, status);
    }

    /** Envía a un usuario concreto (notificación PERSONAL). */
    public void sendToUser(UUID userId, NotificationResponse payload) {
        var sessions = sessionsByUser.get(userId);
        if (sessions == null || sessions.isEmpty()) return;
        TextMessage msg = serialize(payload);
        if (msg != null) sessions.forEach(s -> trySend(s, msg));
    }

    /** Envía a TODOS los conectados (notificación GLOBAL). */
    public void sendToAll(NotificationResponse payload) {
        TextMessage msg = serialize(payload);
        if (msg == null) return;
        sessionsByUser.values().forEach(set -> set.forEach(s -> trySend(s, msg)));
    }

    private TextMessage serialize(NotificationResponse payload) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("No se pudo serializar notificación: {}", e.getMessage());
            return null;
        }
    }

    private void trySend(WebSocketSession session, TextMessage msg) {
        try {
            if (session.isOpen()) session.sendMessage(msg);
        } catch (IOException e) {
            log.debug("Error enviando a sesión {}: {}", session.getId(), e.getMessage());
        }
    }

    private UUID userIdOf(WebSocketSession session) {
        Object attr = session.getAttributes().get("userId");
        return attr instanceof UUID u ? u : null;
    }

    private void silentClose(WebSocketSession session, CloseStatus status) {
        try { session.close(status); } catch (IOException ignored) {}
    }
}
