package com.mascotacare.followup.service.service;

import com.mascotacare.followup.service.entity.Followup;
import com.mascotacare.followup.service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** Componente que dispara notificaciones cuando un seguimiento indica deterioro. */
@Slf4j
@Component
public class AlertTrigger {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public AlertTrigger(RestClient.Builder builder, DiscoveryClient discovery) {
        this.http = builder.build();
        this.discovery = discovery;
    }

    public boolean trigger(Followup f) {
        try {
            String baseUrl = discovery.getInstances("notification-service").stream()
                    .findFirst()
                    .map(s -> s.getUri().toString())
                    .orElse(null);
            if (baseUrl == null) {
                log.warn("notification-service no registrado en Eureka, alerta no enviada");
                return false;
            }
            UserContext.User u = UserContext.get();
            http.post()
                    .uri(baseUrl + "/api/notifications/send")
                    .header("X-User-Id", u == null ? "system" : u.id())
                    .header("X-User-Role", u == null ? "SYSTEM" : u.role())
                    .body(Map.of(
                            "destinatario", f.getIdMascota().toString(),
                            "canal", "EMAIL",
                            "asunto", "Seguimiento sin mejora",
                            "contenido", "El seguimiento " + f.getId() + " indica que la mascota no mejoró."
                    ))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("error enviando alerta para seguimiento {}: {}", f.getId(), e.getMessage());
            return false;
        }
    }
}
