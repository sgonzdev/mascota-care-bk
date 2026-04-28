package com.mascotacare.symptom.service.dto;

import java.util.UUID;

public record TriageFlowResponse(
        UUID idSintoma,
        String nivelUrgencia,
        String accionRecomendada,
        UUID idReglaAplicada,
        Object detalleSintoma
) {}
