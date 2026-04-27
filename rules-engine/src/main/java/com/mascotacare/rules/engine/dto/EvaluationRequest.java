package com.mascotacare.rules.engine.dto;

import com.mascotacare.rules.engine.entity.Rule;
import jakarta.validation.constraints.*;

import java.util.List;

public record EvaluationRequest(
        @NotNull Rule.EspecieAplica especie,
        @NotNull @Min(0) Integer edadMeses,
        @NotEmpty List<String> codigosSintomas
) {}
