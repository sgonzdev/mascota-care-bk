package com.mascotacare.followup.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Notif PERSONAL desde followup-service. Fire-and-forget. */
@Slf4j
@Component
public class NotificationClient {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public NotificationClient(DiscoveryClient discovery) {
        this.http = RestClient.builder().build();
        this.discovery = discovery;
    }

    public void sendPersonal(UUID idUsuario, String asunto, String contenido) {
        String baseUrl = discovery.getInstances("notification-service").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
        if (baseUrl == null) return;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("destinatario", idUsuario.toString());
            body.put("canal", "PUSH");
            body.put("asunto", asunto);
            body.put("contenido", contenido);
            body.put("idUsuario", idUsuario.toString());
            http.post()
                    .uri(baseUrl + "/api/notifications/send")
                    .header("X-User-Id", "system")
                    .header("X-User-Role", "SYSTEM")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("Error notif personal: {}", e.getMessage());
        }
    }
}
