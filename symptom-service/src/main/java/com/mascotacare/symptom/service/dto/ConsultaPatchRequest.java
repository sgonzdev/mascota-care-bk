package com.mascotacare.symptom.service.dto;

import com.mascotacare.symptom.service.entity.Consulta;
import jakarta.validation.constraints.Size;

public record ConsultaPatchRequest(
        Consulta.EstadoConsulta estado,
        @Size(max = 5000) String notasInternas
) {}
