package com.mascotacare.rules.engine.dto;

import com.mascotacare.rules.engine.entity.Rule;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RuleResponse(
        UUID id,
        String condicionSintoma,
        Rule.EspecieAplica especieAplica,
        Integer edadMinMeses,
        Integer edadMaxMeses,
        Rule.NivelUrgencia nivelUrgenciaResultado,
        String accionRecomendada,
        Integer prioridad,
        Boolean activa,
        OffsetDateTime creadaEn,
        OffsetDateTime actualizadaEn
) {}
