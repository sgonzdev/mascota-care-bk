package com.mascotacare.symptom.service.controller;

import com.mascotacare.symptom.service.dto.ConsultaPatchRequest;
import com.mascotacare.symptom.service.dto.ConsultaResponse;
import com.mascotacare.symptom.service.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<ConsultaResponse> list(
            @Parameter(description = "Filtrar por dueño (opcional)")
            @RequestParam(required = false) UUID idUsuario,
            @Parameter(description = "Estado: activa|resuelta|archivada|pendiente|all", example = "all")
            @RequestParam(required = false) String estado) {
        return service.list(idUsuario, estado);
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
