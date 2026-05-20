package com.mascotacare.notification.service.websocket;

import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Punto único para empujar notificaciones por WS. Decide a quién enviar según
 * el scope: GLOBAL → todos los conectados; PERSONAL → solo el idUsuario.
 */
@Component
@RequiredArgsConstructor
public class NotificationBroadcaster {

    private final NotificationWebSocketHandler handler;

    public void broadcast(NotificationResponse n) {
        if (n.scope() == Notification.Scope.GLOBAL) {
            handler.sendToAll(n);
        } else if (n.idUsuario() != null) {
            handler.sendToUser(n.idUsuario(), n);
        }
    }
}
