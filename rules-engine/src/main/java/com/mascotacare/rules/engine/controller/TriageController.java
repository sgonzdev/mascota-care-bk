package com.mascotacare.rules.engine.controller;

import com.mascotacare.rules.engine.dto.EvaluationRequest;
import com.mascotacare.rules.engine.dto.EvaluationResult;
import com.mascotacare.rules.engine.service.RulesEvaluator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
@Tag(name = "Motor de Triage (UC3)",
        description = "Evalúa síntomas normalizados contra el catálogo de reglas activas. "
                + "Lee desde cache Redis (Cache-Aside Pattern §C4-Patrones).")
public class TriageController {

    private final RulesEvaluator evaluator;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluar nivel de urgencia",
            description = "Filtra reglas por especie + edad, busca match por código de síntoma, "
                    + "y devuelve la regla de mayor prioridad disparada. "
                    + "Si no hay match, devuelve nivel MEDIA con recomendación de consulta veterinaria.")
    @ApiResponse(responseCode = "200", description = "Evaluación completada (siempre 200)")
    public EvaluationResult evaluate(@Valid @RequestBody EvaluationRequest req) {
        return evaluator.evaluate(req);
    }
}
