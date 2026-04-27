package com.mascotacare.notification.service.provider;

import com.mascotacare.notification.service.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsProvider implements NotificationProvider {

    @Value("${notification.twilio.sid:}")
    private String twilioSid;

    @Override public Notification.Canal supports() { return Notification.Canal.SMS; }

    @Override
    public void send(String destinatario, String asunto, String contenido) {
        if (twilioSid == null || twilioSid.isBlank()) {
            log.info("[SMS-MOCK] → {} | {}", destinatario, contenido);
            return;
        }
        // TODO: Twilio cuando existan credenciales.
        log.info("[SMS-TWILIO] → {} (cliente real pendiente)", destinatario);
    }
}
