package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente para registrar eventos en metrics-service (§C4 Tabla 9 — Orquestación).
 * Fire-and-forget: si la llamada falla solo loggeamos, no rompemos el flujo principal.
 */
@Slf4j
@Component
public class MetricsClient {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public MetricsClient(DiscoveryClient discovery) {
        this.http = RestClient.builder().build();
        this.discovery = discovery;
    }

    public void record(String evento, String etiqueta, long valor) {
        String baseUrl = discovery.getInstances("metrics-service").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
        if (baseUrl == null) {
            log.debug("metrics-service no disponible, evento {} descartado", evento);
            return;
        }
        try {
            UserContext.User user = UserContext.get();
            Map<String, Object> body = new HashMap<>();
            body.put("evento", evento);
            body.put("valor", valor);
            if (etiqueta != null) body.put("etiqueta", etiqueta);
            http.post()
                    .uri(baseUrl + "/api/metrics/events")
                    .header("X-User-Id", user == null ? "system" : user.id())
                    .header("X-User-Role", user == null ? "SYSTEM" : user.role())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("Error registrando métrica {}: {}", evento, e.getMessage());
        }
    }
}
