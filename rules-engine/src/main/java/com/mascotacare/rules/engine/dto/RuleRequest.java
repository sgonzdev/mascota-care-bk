package com.mascotacare.rules.engine.dto;

import com.mascotacare.rules.engine.entity.Rule;
import jakarta.validation.constraints.*;

public record RuleRequest(
        @NotBlank @Size(max = 200) String condicionSintoma,
        @NotNull Rule.EspecieAplica especieAplica,
        @NotNull @Min(0) @Max(360) Integer edadMinMeses,
        @NotNull @Min(0) @Max(360) Integer edadMaxMeses,
        @NotNull Rule.NivelUrgencia nivelUrgenciaResultado,
        @NotBlank @Size(min = 10, max = 2000) String accionRecomendada,
        @NotNull @Min(1) @Max(10) Integer prioridad,
        @NotNull Boolean activa
) {}
