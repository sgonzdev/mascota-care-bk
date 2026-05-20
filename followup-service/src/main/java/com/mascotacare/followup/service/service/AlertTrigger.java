package com.mascotacare.followup.service.service;

import com.mascotacare.followup.service.entity.Followup;
import com.mascotacare.followup.service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            String notifUrl = discoverUrl("notification-service");
            if (notifUrl == null) {
                log.warn("notification-service no registrado, alerta no enviada");
                return false;
            }
            UserContext.User u = UserContext.get();
            UUID dueñoId = lookupOwner(f.getIdMascota(), u);
            var body = new HashMap<String, Object>();
            body.put("destinatario", dueñoId != null ? dueñoId.toString() : f.getIdMascota().toString());
            body.put("canal", "PUSH");
            body.put("asunto", "Tu mascota no ha mejorado");
            body.put("contenido", "El seguimiento de tu mascota indica que su estado no mejoró. "
                    + "Te recomendamos acudir al veterinario lo antes posible.");
            if (dueñoId != null) body.put("idUsuario", dueñoId.toString());

            http.post()
                    .uri(notifUrl + "/api/notifications/send")
                    .header("X-User-Id", u == null ? "system" : u.id())
                    .header("X-User-Role", u == null ? "SYSTEM" : u.role())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("error enviando alerta para seguimiento {}: {}", f.getId(), e.getMessage());
            return false;
        }
    }

    /** Pregunta a pet-service por el dueño de la mascota. Null si no se encuentra. */
    private UUID lookupOwner(UUID idMascota, UserContext.User u) {
        try {
            String petUrl = discoverUrl("pet-service");
            if (petUrl == null) return null;
            Map<?, ?> pet = http.get()
                    .uri(petUrl + "/api/pets/" + idMascota)
                    .header("X-User-Id", u == null ? "system" : u.id())
                    .header("X-User-Role", u == null ? "SYSTEM" : u.role())
                    .retrieve()
                    .body(Map.class);
            if (pet == null) return null;
            Object idUsuario = pet.get("idUsuario");
            return idUsuario != null ? UUID.fromString(idUsuario.toString()) : null;
        } catch (Exception e) {
            log.debug("No se pudo obtener idUsuario de mascota {}: {}", idMascota, e.getMessage());
            return null;
        }
    }

    private String discoverUrl(String serviceId) {
        return discovery.getInstances(serviceId).stream()
                .findFirst()
                .map(s -> s.getUri().toString())
                .orElse(null);
    }
}
