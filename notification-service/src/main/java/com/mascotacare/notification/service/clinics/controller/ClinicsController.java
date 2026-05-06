package com.mascotacare.notification.service.clinics.controller;

import com.mascotacare.notification.service.clinics.dto.ClinicDto;
import com.mascotacare.notification.service.clinics.service.ClinicsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clínicas Veterinarias",
        description = "Búsqueda geoespacial de veterinarias cercanas (UC5). "
                + "Usa Google Places si hay GOOGLE_MAPS_API_KEY, fallback a OpenStreetMap.")
public class ClinicsController {

    private final ClinicsService service;

    @GetMapping("/nearby")
    @Operation(
            summary = "Buscar clínicas veterinarias cercanas",
            description = "Devuelve hasta `limit` clínicas dentro del radio especificado, "
                    + "ordenadas por distancia ascendente. Útil para UC5 cuando se detecta "
                    + "una urgencia ALTA y hay que derivar al dueño a un veterinario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clínicas (puede ser vacía)",
                    content = @Content(schema = @Schema(implementation = ClinicDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos (lat/lng fuera de rango)"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    public List<ClinicDto> nearby(
            @Parameter(description = "Latitud de origen (-90 a 90)", example = "40.4168", required = true)
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @Parameter(description = "Longitud de origen (-180 a 180)", example = "-3.7038", required = true)
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng,
            @Parameter(description = "Radio de búsqueda en metros (100-50000)", example = "3000")
            @RequestParam(defaultValue = "5000") @Min(100) @Max(50000) int radius,
            @Parameter(description = "Número máximo de resultados (1-20)", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit) {
        return service.findNearby(lat, lng, radius, limit);
    }
}
