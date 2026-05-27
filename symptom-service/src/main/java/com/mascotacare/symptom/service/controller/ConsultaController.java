package com.mascotacare.symptom.service.controller;

import com.mascotacare.symptom.service.dto.ConsultaPatchRequest;
import com.mascotacare.symptom.service.dto.ConsultaResponse;
import com.mascotacare.symptom.service.dto.PageResponse;
import com.mascotacare.symptom.service.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultas (UC2+UC3)",
        description = "Agregaciones persistidas por cada triage. Permite al frontend listar el "
                + "histórico, ver detalle, actualizar estado/notas y eliminar.")
public class ConsultaController {

    private final ConsultaService service;

    @GetMapping
    @Operation(summary = "Listar consultas",
            description = "Filtros opcionales por id de usuario y estado (activa|resuelta|archivada|pendiente|all).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista (puede ser vacía)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public PageResponse<ConsultaResponse> list(
            @Parameter(description = "Filtrar por dueño (opcional)")
            @RequestParam(required = false) UUID idUsuario,
            @Parameter(description = "Estado: activa|resuelta|archivada|pendiente|all", example = "all")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Urgencia: ALTA|MEDIA|BAJA|all (lo usa el rol VETERINARIO)")
            @RequestParam(required = false) String urgencia,
            Pageable pageable) {
        // El VETERINARIO solo ve casos sin asignar o asignados a él (cualquier urgencia).
        com.mascotacare.symptom.service.security.UserContext.User u =
                com.mascotacare.symptom.service.security.UserContext.get();
        UUID vetId = null;
        if (u != null && "VETERINARIO".equalsIgnoreCase(u.role())) {
            try { vetId = UUID.fromString(u.id()); } catch (Exception ignore) {}
        }
        return service.list(idUsuario, estado, urgencia, vetId, pageable);
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/claim")
    @Operation(summary = "Veterinario toma el caso (RF22 — claim exclusivo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignado"),
            @ApiResponse(responseCode = "403", description = "Solo veterinarios"),
            @ApiResponse(responseCode = "409", description = "Ya asignado a otro vet")
    })
    public ConsultaResponse claim(@PathVariable UUID id) {
        UUID vet = requireVetId();
        com.mascotacare.symptom.service.security.UserContext.User u =
                com.mascotacare.symptom.service.security.UserContext.get();
        try {
            return service.claim(id, vet, u.name(), u.email());
        } catch (IllegalStateException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/release")
    @Operation(summary = "Veterinario suelta el caso (queda disponible para otros)")
    public ConsultaResponse release(@PathVariable UUID id) {
        UUID vet = requireVetId();
        try {
            return service.release(id, vet);
        } catch (IllegalStateException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, e.getMessage());
        }
    }

    private UUID requireVetId() {
        com.mascotacare.symptom.service.security.UserContext.User u =
                com.mascotacare.symptom.service.security.UserContext.get();
        if (u == null || !"VETERINARIO".equalsIgnoreCase(u.role())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Solo veterinarios");
        }
        try { return UUID.fromString(u.id()); } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Sesión inválida");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una consulta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public ConsultaResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar estado o notas internas",
            description = "Sólo se modifican los campos presentes en el body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "400", description = "Body inválido")
    })
    public ConsultaResponse patch(@PathVariable UUID id, @Valid @RequestBody ConsultaPatchRequest req) {
        return service.patch(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar consulta (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
