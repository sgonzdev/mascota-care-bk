package com.mascotacare.guide.service.controller;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.dto.GuideResponse;
import com.mascotacare.guide.service.service.GuideService;
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
@RequestMapping("/api/guides")
@RequiredArgsConstructor
@Tag(name = "Guías de Cuidado (UC4)",
        description = "Genera guías personalizadas (cuidado, alimentación, alarma) para una mascota. "
                + "Si MISTRAL_API_KEY está configurada → enriquece con IA; si no → plantilla estática.")
public class GuideController {

    private final GuideService service;

    @PostMapping("/generate")
    @Operation(summary = "Generar guía personalizada",
            description = "Construye plantilla baseline según tipo+especie+raza+edad y la enriquece con LLM "
                    + "(Mistral Large) si hay MISTRAL_API_KEY. El campo `fuente` indica TEMPLATE o AI.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Guía generada y persistida"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (tipo/especie inválido)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public ResponseEntity<GuideResponse> generate(@Valid @RequestBody GuideRequest req) {
        GuideResponse out = service.generate(req);
        return ResponseEntity.created(URI.create("/api/guides/" + out.id())).body(out);
    }

    @GetMapping("/by-pet/{idMascota}")
    @Operation(summary = "Historial de guías de una mascota",
            description = "Devuelve todas las guías generadas para esa mascota, más recientes primero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de guías (puede ser vacía)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public List<GuideResponse> history(@PathVariable UUID idMascota) {
        return service.historyOf(idMascota);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una guía por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guía encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "JWT ausente")
    })
    public GuideResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
