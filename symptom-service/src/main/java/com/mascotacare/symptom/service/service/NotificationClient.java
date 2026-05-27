package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente para enviar notificaciones desde symptom-service. Usado por
 * {@link ConsultaService} cuando un triage resulta en urgencia ALTA (RF13).
 * Fire-and-forget: errores se loguean, no se propagan al usuario.
 */
@Slf4j
@Component
public class NotificationClient {

    private final RestClient http;
    private final DiscoveryClient discovery;

    public NotificationClient(DiscoveryClient discovery) {
        this.http = RestClient.builder().build();
        this.discovery = discovery;
    }

    /** Notifica PERSONAL a un usuario (su mascota, su consulta). */
    public void sendPersonal(UUID idUsuario, String asunto, String contenido) {
        send(buildBody(idUsuario.toString(), asunto, contenido, idUsuario));
    }

    /**
     * Envía la misma notificación PERSONAL a cada veterinario registrado.
     * Equivalente a un broadcast pero sin exponerlo al público — los dueños
     * no ven mensajes con ids internos de casos clínicos.
     */
    public void notifyAllVets(String asunto, String contenido) {
        String gw = discovery.getInstances("api-gateway").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
        if (gw == null) return;
        try {
            UserContext.User u = UserContext.get();
            // Usamos role=ADMIN para listar vets (endpoint admin-only). Los micros
            // internos confían entre sí por la red privada del cluster.
            Map<?, ?>[] vets = http.get()
                    .uri(gw + "/auth/users?rol=VETERINARIO")
                    .header("X-User-Id", u == null ? "system" : u.id())
                    .header("X-User-Role", "ADMIN")
                    .retrieve()
                    .body(Map[].class);
            if (vets == null) return;
            for (Map<?, ?> vet : vets) {
                try {
                    UUID vetId = UUID.fromString(String.valueOf(vet.get("id")));
                    sendPersonal(vetId, asunto, contenido);
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            log.debug("Error notificando a vets: {}", e.getMessage());
        }
    }

    private void send(Map<String, Object> body) {
        String baseUrl = resolveUrl();
        if (baseUrl == null) return;
        try {
            UserContext.User u = UserContext.get();
            http.post()
                    .uri(baseUrl + "/api/notifications/send")
                    .header("X-User-Id", u == null ? "system" : u.id())
                    .header("X-User-Role", u == null ? "SYSTEM" : u.role())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("Error enviando notif personal: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildBody(String destinatario, String asunto, String contenido, UUID idUsuario) {
        Map<String, Object> body = new HashMap<>();
        body.put("destinatario", destinatario);
        body.put("canal", "PUSH");
        body.put("asunto", asunto);
        body.put("contenido", contenido);
        body.put("idUsuario", idUsuario.toString());
        return body;
    }

    private String resolveUrl() {
        return discovery.getInstances("notification-service").stream()
                .findFirst().map(s -> s.getUri().toString()).orElse(null);
    }
}
