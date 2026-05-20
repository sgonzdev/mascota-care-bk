package com.mascotacare.followup.service.controller;

import com.mascotacare.followup.service.dto.FollowupRequest;
import com.mascotacare.followup.service.dto.FollowupResponse;
import com.mascotacare.followup.service.dto.PageResponse;
import com.mascotacare.followup.service.service.FollowupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/followups")
@RequiredArgsConstructor
@Tag(name = "Seguimientos (UC6)",
        description = "Registro post-consulta del estado de la mascota (mejoró/no mejoró/sin dato). "
                + "Si NO_MEJORO, dispara automáticamente alerta al Notification Service. "
                + "Un FollowupScheduler revisa cada 5 min los pendientes sin mejora >48h.")
public class FollowupController {

    private final FollowupService service;

    @PostMapping
    @Operation(summary = "Registrar seguimiento",
            description = "Si estado es NO_MEJORO, automáticamente envía alerta vía notification-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seguimiento registrado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (estado inválido)")
    })
    public ResponseEntity<FollowupResponse> register(@Valid @RequestBody FollowupRequest req) {
        FollowupResponse out = service.register(req);
        return ResponseEntity.created(URI.create("/api/followups/" + out.id())).body(out);
    }

    @GetMapping("/by-pet/{idMascota}")
    @Operation(summary = "Historial paginado de seguimientos",
            description = "Ordenados por fecha descendente. Usa `?page=0&size=20`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de seguimientos"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public PageResponse<FollowupResponse> history(@PathVariable UUID idMascota, Pageable pageable) {
        return service.historyOf(idMascota, pageable);
    }

    @GetMapping("/by-consultation/{idConsulta}")
    @Operation(summary = "Seguimientos de una consulta",
            description = "Devuelve todos los seguimientos asociados a una consulta, "
                    + "ordenados por fecha descendente. Sin paginación (suelen ser pocos).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista (puede ser vacía)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public java.util.List<FollowupResponse> byConsultation(@PathVariable UUID idConsulta) {
        return service.byConsulta(idConsulta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un seguimiento por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public FollowupResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
