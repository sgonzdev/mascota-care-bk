package com.mascotacare.symptom.service.controller;

import com.mascotacare.symptom.service.dto.SymptomRequest;
import com.mascotacare.symptom.service.dto.SymptomResponse;
import com.mascotacare.symptom.service.dto.TriageFlowRequest;
import com.mascotacare.symptom.service.dto.TriageFlowResponse;
import com.mascotacare.symptom.service.service.SymptomService;
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
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@Tag(name = "Síntomas (UC2)",
        description = "Recepción y normalización de síntomas reportados por el dueño. "
                + "El SymptomNormalizer convierte texto libre a códigos canónicos.")
public class SymptomController {

    private final SymptomService service;

    @PostMapping
    @Operation(summary = "Registrar síntomas (sin disparar triage)",
            description = "Persiste el reporte y normaliza el texto a códigos. NO llama al motor de reglas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Síntoma registrado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (descripción muy corta)")
    })
    public ResponseEntity<SymptomResponse> register(@Valid @RequestBody SymptomRequest req) {
        SymptomResponse created = service.register(req);
        return ResponseEntity.created(URI.create("/api/symptoms/" + created.id())).body(created);
    }

    @PostMapping("/triage")
    @Operation(summary = "Flujo orquestado UC2+UC3: registra y evalúa urgencia",
            description = "Endpoint conveniencia para el frontend: en una sola llamada persiste el síntoma "
                    + "Y dispara el Rules Engine para devolver el veredicto (ALTA/MEDIA/BAJA) + acción.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Triage completado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida"),
            @ApiResponse(responseCode = "503", description = "Rules Engine inalcanzable (devuelve fallback MEDIA)")
    })
    public TriageFlowResponse triage(@Valid @RequestBody TriageFlowRequest req) {
        return service.registerAndEvaluate(req);
    }

    @GetMapping("/by-pet/{idMascota}")
    @Operation(summary = "Historial de síntomas de una mascota (más recientes primero)")
    public List<SymptomResponse> history(@PathVariable UUID idMascota) {
        return service.historyOf(idMascota);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un síntoma por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public SymptomResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }
}
