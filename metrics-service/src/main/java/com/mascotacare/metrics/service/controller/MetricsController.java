package com.mascotacare.metrics.service.controller;

import com.mascotacare.metrics.service.dto.DashboardResponse;
import com.mascotacare.metrics.service.dto.EventRequest;
import com.mascotacare.metrics.service.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas (UC7 — Admin)",
        description = "Time-series sobre Redis. Otros microservicios postean eventos; "
                + "el dashboard agrega los KPIs vía KPICalculator (§C4).")
public class MetricsController {

    private final MetricsService service;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Registrar evento (fire-and-forget)",
            description = "Endpoint asíncrono. Otros servicios lo invocan al ocurrir un evento "
                    + "(consulta creada, urgencia, mejora, regla disparada).")
    @ApiResponse(responseCode = "202", description = "Evento aceptado y agregado en Redis")
    public void recordEvent(@Valid @RequestBody EventRequest req) {
        service.recordEvent(req);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener KPIs del dashboard",
            description = "Devuelve total, distribución por urgencia, tasa de mejora, "
                    + "consultas por día y top reglas disparadas.")
    @ApiResponse(responseCode = "200", description = "Dashboard completo")
    public DashboardResponse dashboard(
            @Parameter(description = "Ventana temporal en días (1-365)", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        return service.buildDashboard(days);
    }
}
