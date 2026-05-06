package com.mascotacare.rules.engine.controller;

import com.mascotacare.rules.engine.dto.RuleRequest;
import com.mascotacare.rules.engine.dto.RuleResponse;
import com.mascotacare.rules.engine.service.RuleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Tag(name = "Reglas (UC8 — Admin)",
        description = "CRUD del catálogo de reglas del motor de triage. "
                + "Cada modificación invalida el cache Redis (Cache-Aside Pattern §C4).")
public class RuleAdminController {

    private final RuleAdminService service;

    @PostMapping
    @Operation(summary = "Crear nueva regla", description = "Solo administradores. Invalida el cache.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Regla creada"),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    public ResponseEntity<RuleResponse> create(@Valid @RequestBody RuleRequest req) {
        RuleResponse out = service.create(req);
        return ResponseEntity.created(URI.create("/api/rules/" + out.id())).body(out);
    }

    @GetMapping
    @Operation(summary = "Listar todas las reglas (activas e inactivas)",
            description = "Devuelve el catálogo completo. El frontend admin lo usa para mostrar la tabla de gestión.")
    @ApiResponse(responseCode = "200", description = "Lista de reglas")
    public List<RuleResponse> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una regla por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public RuleResponse get(@PathVariable UUID id) { return service.findById(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar regla existente",
            description = "Sustituye TODOS los campos. Invalida cache.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regla actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    public RuleResponse update(@PathVariable UUID id, @Valid @RequestBody RuleRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Activar/desactivar regla",
            description = "Conmuta el flag `activa`. Invalida el cache para que el siguiente triage la considere/excluya.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regla actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public RuleResponse toggle(@PathVariable UUID id) { return service.toggle(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar regla del catálogo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
