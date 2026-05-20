package com.mascotacare.notification.service.controller;

import com.mascotacare.notification.service.dto.BroadcastRequest;
import com.mascotacare.notification.service.dto.NotificationRequest;
import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.dto.PageResponse;
import com.mascotacare.notification.service.security.UserContext;
import com.mascotacare.notification.service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones (UC5)",
        description = "Envío multi-canal: PUSH (FCM), EMAIL (SendGrid), SMS (Twilio). "
                + "Sin credenciales configuradas → modo MOCK con log estructurado. "
                + "RetryHandler con backoff exponencial 200ms→400ms→800ms (§C4). "
                + "Push en tiempo real vía WebSocket /ws/notifications.")
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/send")
    @Operation(summary = "Enviar notificación PERSONAL por canal específico",
            description = "Persiste en BD, intenta envío con reintentos exponenciales. "
                    + "Devuelve la entidad con estado final (ENVIADA / FALLIDA / PENDIENTE) y nº de intentos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificación procesada"),
            @ApiResponse(responseCode = "400", description = "Validación fallida"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido")
    })
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest req) {
        NotificationResponse out = service.send(req);
        return ResponseEntity.created(URI.create("/api/notifications/" + out.id())).body(out);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Anuncio GLOBAL (solo admin)",
            description = "Crea una notificación visible para todos los usuarios y la empuja "
                    + "por WebSocket a todos los conectados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anuncio creado y emitido"),
            @ApiResponse(responseCode = "403", description = "Solo admin"),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    public ResponseEntity<NotificationResponse> broadcast(@Valid @RequestBody BroadcastRequest req) {
        if (!isAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores");
        NotificationResponse out = service.broadcast(req);
        return ResponseEntity.created(URI.create("/api/notifications/" + out.id())).body(out);
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones del usuario actual",
            description = "Admin ve todo. Usuario normal ve GLOBALES + sus PERSONALES. "
                    + "Paginado: `?page=0&size=20`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de notificaciones"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public PageResponse<NotificationResponse> list(Pageable pageable) {
        UserContext.User u = UserContext.get();
        UUID userId = u != null ? UUID.fromString(u.id()) : null;
        boolean admin = u != null && u.role() != null && u.role().equalsIgnoreCase("ADMIN");
        return service.listFor(userId, admin, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una notificación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public NotificationResponse get(@PathVariable UUID id) { return service.findById(id); }

    private boolean isAdmin() {
        UserContext.User u = UserContext.get();
        return u != null && u.role() != null && u.role().equalsIgnoreCase("ADMIN");
    }
}
