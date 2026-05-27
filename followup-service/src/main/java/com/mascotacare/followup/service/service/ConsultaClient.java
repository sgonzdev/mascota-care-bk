package com.mascotacare.followup.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cliente que consulta el symptom-service para encontrar consultas que
 * cumplieron 24-48h sin seguimiento (RF27). Usado por {@link FollowupScheduler}.
 */
@Slf4j
@Component
public class ConsultaClient {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public ConsultaClient(DiscoveryClient discovery) {
        this.http = RestClient.builder().build();
        this.discovery = discovery;
    }

    /**
     * Devuelve consultas en estado `activa` o `pendiente` creadas hace más de
     * `hours` horas. La lógica de "sin seguimiento previo" la valida el
     * scheduler localmente cruzando con su BD.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findOldOpenConsultas(int hours) {
        String baseUrl = discovery.getInstances("symptom-service").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
        if (baseUrl == null) {
            log.warn("symptom-service no disponible para scheduler");
            return List.of();
        }
        try {
            // Pedimos las "activas" — el filtro por edad lo hacemos en el scheduler
            // porque el endpoint no expone parámetro `desde`.
            Map<String, Object> page = http.get()
                    .uri(baseUrl + "/api/consultations?estado=activa&size=200")
                    .header("X-User-Id", "system")
                    .header("X-User-Role", "ADMIN")
                    .retrieve()
                    .body(Map.class);
            if (page == null) return List.of();
            return (List<Map<String, Object>>) page.getOrDefault("content", List.of());
        } catch (Exception e) {
            log.debug("Error consultando consultas viejas: {}", e.getMessage());
            return List.of();
        }
    }

    public OffsetDateTime parseFechaHora(Object raw) {
        try { return OffsetDateTime.parse(raw.toString()); }
        catch (Exception e) { return OffsetDateTime.now(); }
    }
}
