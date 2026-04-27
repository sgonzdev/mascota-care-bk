package com.mascotacare.notification.service.service;

import com.mascotacare.notification.service.dto.NotificationRequest;
import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.entity.Notification;
import com.mascotacare.notification.service.provider.NotificationProvider;
import com.mascotacare.notification.service.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final int MAX_RETRIES = 3;
    private final NotificationRepository repository;
    private final List<NotificationProvider> providers;

    public NotificationResponse send(NotificationRequest req) {
        Notification entity = repository.save(Notification.builder()
                .destinatario(req.destinatario())
                .canal(req.canal())
                .asunto(req.asunto())
                .contenido(req.contenido())
                .estado(Notification.Estado.PENDIENTE)
                .intentos(0)
                .build());

        Map<Notification.Canal, NotificationProvider> map = providers.stream()
                .collect(Collectors.toMap(NotificationProvider::supports, p -> p));
        NotificationProvider provider = map.get(req.canal());
        if (provider == null) throw new IllegalStateException("Sin proveedor para canal " + req.canal());

        attemptSend(entity, provider);
        return toResponse(entity);
    }

    private void attemptSend(Notification entity, NotificationProvider provider) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            entity.setIntentos(entity.getIntentos() + 1);
            try {
                provider.send(entity.getDestinatario(), entity.getAsunto(), entity.getContenido());
                entity.setEstado(Notification.Estado.ENVIADA);
                entity.setEnviadaEn(OffsetDateTime.now());
                entity.setErrorMessage(null);
                repository.save(entity);
                return;
            } catch (Exception e) {
                log.warn("intento {} falló para notif {}: {}", entity.getIntentos(), entity.getId(), e.getMessage());
                entity.setErrorMessage(e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    try { Thread.sleep(200L * (i + 1)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        entity.setEstado(Notification.Estado.FALLIDA);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificación " + id + " no encontrada")));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getDestinatario(), n.getCanal(), n.getAsunto(), n.getContenido(),
                n.getEstado(), n.getIntentos(), n.getErrorMessage(),
                n.getCreadaEn(), n.getEnviadaEn());
    }
}
