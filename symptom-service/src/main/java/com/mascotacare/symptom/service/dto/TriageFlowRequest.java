package com.mascotacare.symptom.service.dto;

import com.mascotacare.symptom.service.entity.Symptom;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Request del flujo orquestado UC2+UC3: ingresa síntomas y devuelve
 * la evaluación de urgencia en una sola llamada.
 */
public record TriageFlowRequest(
        @NotNull UUID idMascota,
        @NotBlank @Size(max = 10) String especie,
        @NotNull @Min(0) Integer edadMeses,
        @NotBlank @Size(min = 5, max = 2000) String descripcionLibre,
        Symptom.Severidad severidadPercibida
) {}
