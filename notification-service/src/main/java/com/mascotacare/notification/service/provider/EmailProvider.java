package com.mascotacare.notification.service.provider;

import com.mascotacare.notification.service.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailProvider implements NotificationProvider {

    @Value("${notification.email.api-key:}")
    private String apiKey;

    @Override public Notification.Canal supports() { return Notification.Canal.EMAIL; }

    @Override
    public void send(String destinatario, String asunto, String contenido) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("[EMAIL-MOCK] To: {} | Subject: {}\n--------\n{}\n--------",
                    destinatario, asunto, contenido);
            return;
        }
        // TODO: SendGrid / Amazon SES cuando exista la API key.
        log.info("[EMAIL-SENDGRID] → {} (cliente real pendiente)", destinatario);
    }
}
