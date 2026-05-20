package com.mascotacare.notification.service.service;

import com.mascotacare.notification.service.dto.BroadcastRequest;
import com.mascotacare.notification.service.dto.NotificationRequest;
import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.dto.PageResponse;
import com.mascotacare.notification.service.entity.Notification;
import com.mascotacare.notification.service.provider.NotificationProvider;
import com.mascotacare.notification.service.repository.NotificationRepository;
import com.mascotacare.notification.service.websocket.NotificationBroadcaster;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    private final NotificationRepository repository;
    private final List<NotificationProvider> providers;
    private final RetryHandler retryHandler;
    private final NotificationBroadcaster broadcaster;

    /** Envío PERSONAL — alerta a un usuario específico (por seguimiento, urgencia, etc). */
    public NotificationResponse send(NotificationRequest req) {
        Notification entity = repository.save(Notification.builder()
                .destinatario(req.destinatario())
                .canal(req.canal())
                .asunto(req.asunto())
                .contenido(req.contenido())
                .estado(Notification.Estado.PENDIENTE)
                .intentos(0)
                .scope(req.idUsuario() != null ? Notification.Scope.PERSONAL : Notification.Scope.GLOBAL)
                .idUsuario(req.idUsuario())
                .build());

        Map<Notification.Canal, NotificationProvider> map = providers.stream()
                .collect(Collectors.toMap(NotificationProvider::supports, p -> p));
        NotificationProvider provider = map.get(req.canal());
        if (provider == null) throw new IllegalStateException("Sin proveedor para canal " + req.canal());

        attemptSend(entity, provider);
        NotificationResponse response = toResponse(entity);
        broadcaster.broadcast(response);
        return response;
    }

    /** Anuncio GLOBAL — visible para todos los usuarios. */
    public NotificationResponse broadcast(BroadcastRequest req) {
        Notification entity = repository.saveAndFlush(Notification.builder()
                .destinatario("ALL")
                .canal(Notification.Canal.PUSH)
                .asunto(req.asunto())
                .contenido(req.contenido())
                .estado(Notification.Estado.ENVIADA)  // los broadcasts in-app no usan provider externo
                .intentos(1)
                .enviadaEn(OffsetDateTime.now())
                .scope(Notification.Scope.GLOBAL)
                .build());
        // saveAndFlush activa @CreationTimestamp, pero el campo en memoria sigue null:
        // recargamos para que la respuesta + el WebSocket emitan la fecha real.
        Notification reloaded = repository.findById(entity.getId()).orElse(entity);
        NotificationResponse response = toResponse(reloaded);
        broadcaster.broadcast(response);
        return response;
    }

    private void attemptSend(Notification entity, NotificationProvider provider) {
        var result = retryHandler.execute(() ->
                provider.send(entity.getDestinatario(), entity.getAsunto(), entity.getContenido()));
        entity.setIntentos(result.intentos());
        if (result.ok()) {
            entity.setEstado(Notification.Estado.ENVIADA);
            entity.setEnviadaEn(OffsetDateTime.now());
            entity.setErrorMessage(null);
        } else {
            entity.setEstado(Notification.Estado.FALLIDA);
            entity.setErrorMessage(result.errorMessage());
        }
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificación " + id + " no encontrada")));
    }

    /**
     * Lista paginada:
     *   - admin: ve todo (globales + personales de cualquiera).
     *   - usuario normal: ve GLOBAL + sus PERSONAL.
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listFor(UUID userId, boolean admin, Pageable pageable) {
        var page = admin
                ? repository.findAllByOrderByCreadaEnDesc(pageable)
                : repository.findByScopeOrIdUsuarioOrderByCreadaEnDesc(
                        Notification.Scope.GLOBAL, userId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getDestinatario(), n.getCanal(), n.getAsunto(), n.getContenido(),
                n.getEstado(), n.getIntentos(), n.getErrorMessage(),
                n.getCreadaEn(), n.getEnviadaEn(),
                n.getScope(), n.getIdUsuario());
    }
}
