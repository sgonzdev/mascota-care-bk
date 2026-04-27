package com.mascotacare.metrics.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotBlank String evento,
        @NotNull Long valor,
        String etiqueta
) {}
