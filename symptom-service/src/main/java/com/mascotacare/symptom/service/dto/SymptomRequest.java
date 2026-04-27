package com.mascotacare.symptom.service.dto;

import com.mascotacare.symptom.service.entity.Symptom;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record SymptomRequest(
        @NotNull UUID idMascota,
        @NotBlank @Size(min = 5, max = 2000) String descripcionLibre,
        Symptom.Severidad severidadPercibida
) {}
