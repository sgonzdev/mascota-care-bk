package com.mascotacare.symptom.service.dto;

import com.mascotacare.symptom.service.entity.Symptom;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SymptomResponse(
        UUID id,
        UUID idMascota,
        String descripcionLibre,
        List<String> codigosNormalizados,
        Symptom.Severidad severidadPercibida,
        OffsetDateTime fechaReporte
) {}
