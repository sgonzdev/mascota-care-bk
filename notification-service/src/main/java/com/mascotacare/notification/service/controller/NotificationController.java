package com.mascotacare.notification.service.controller;

import com.mascotacare.notification.service.dto.NotificationRequest;
import com.mascotacare.notification.service.dto.NotificationResponse;
import com.mascotacare.notification.service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones (UC5)",
        description = "Envío multi-canal: PUSH (FCM), EMAIL (SendGrid), SMS (Twilio). "
                + "Sin credenciales configuradas → modo MOCK con log estructurado. "
                + "RetryHandler con backoff exponencial 200ms→400ms→800ms (§C4).")
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/send")
    @Operation(summary = "Enviar notificación por canal específico",
            description = "Persiste en BD, intenta envío con reintentos exponenciales. "
                    + "Devuelve la entidad con estado final (ENVIADA / FALLIDA / PENDIENTE) y nº de intentos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificación procesada (puede haber fallado el envío externo, ver campo `estado`)"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (canal inválido, sin destinatario o contenido)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido")
    })
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest req) {
        NotificationResponse out = service.send(req);
        return ResponseEntity.created(URI.create("/api/notifications/" + out.id())).body(out);
    }

    @GetMapping
    @Operation(summary = "Listar todas las notificaciones (admin)",
            description = "Devuelve historial completo. Útil para auditoría y debugging.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public List<NotificationResponse> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una notificación por id",
            description = "Incluye campo `estado`, `intentos` y `errorMessage` si falló.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public NotificationResponse get(@PathVariable UUID id) { return service.findById(id); }
}
