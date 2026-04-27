package com.mascotacare.notification.service.provider;

import com.mascotacare.notification.service.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushProvider implements NotificationProvider {

    @Value("${notification.fcm.key:}")
    private String fcmKey;

    @Override public Notification.Canal supports() { return Notification.Canal.PUSH; }

    @Override
    public void send(String destinatario, String asunto, String contenido) {
        if (fcmKey == null || fcmKey.isBlank()) {
            log.info("[PUSH-MOCK] → {} | {} | {}", destinatario, asunto, contenido);
            return;
        }
        // TODO: integración real con FCM/APNs cuando exista la API key.
        log.info("[PUSH-FCM] → {} | {} (cliente real pendiente)", destinatario, asunto);
    }
}
