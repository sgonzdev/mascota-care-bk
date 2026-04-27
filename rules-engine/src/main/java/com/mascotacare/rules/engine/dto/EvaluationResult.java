package com.mascotacare.rules.engine.dto;

import com.mascotacare.rules.engine.entity.Rule;

import java.util.List;
import java.util.UUID;

public record EvaluationResult(
        Rule.NivelUrgencia nivelUrgencia,
        UUID idReglaAplicada,
        String accionRecomendada,
        double confianza,
        List<UUID> reglasDisparadas
) {}
