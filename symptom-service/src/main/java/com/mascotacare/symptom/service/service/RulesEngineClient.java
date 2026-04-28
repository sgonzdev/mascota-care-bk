package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Cliente HTTP para invocar al Rules Engine — usado en orquestación §C4 Tabla 9. */
@Slf4j
@Component
public class RulesEngineClient {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public RulesEngineClient(DiscoveryClient discovery) {
        this.http = RestClient.builder().build();
        this.discovery = discovery;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> evaluate(String especie, int edadMeses, List<String> codigos) {
        String baseUrl = resolveUrl();
        if (baseUrl == null) {
            log.warn("rules-engine no registrado, devolviendo MEDIA por defecto");
            return Map.of("nivelUrgencia", "MEDIA", "accionRecomendada",
                    "Servicio de triage temporalmente indisponible. Consultar veterinario.");
        }
        try {
            UserContext.User user = UserContext.get();
            return http.post()
                    .uri(baseUrl + "/api/triage/evaluate")
                    .header("X-User-Id", user == null ? "system" : user.id())
                    .header("X-User-Role", user == null ? "SYSTEM" : user.role())
                    .body(Map.of("especie", especie, "edadMeses", edadMeses, "codigosSintomas", codigos))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("error invocando rules-engine: {}", e.getMessage());
            return Map.of("nivelUrgencia", "MEDIA", "accionRecomendada",
                    "Error al evaluar urgencia. Consultar veterinario.");
        }
    }

    private String resolveUrl() {
        return discovery.getInstances("rules-engine").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
    }
}
