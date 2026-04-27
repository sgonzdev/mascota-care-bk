package com.mascotacare.rules.engine.service;

import com.mascotacare.rules.engine.cache.RuleCacheManager;
import com.mascotacare.rules.engine.dto.EvaluationRequest;
import com.mascotacare.rules.engine.dto.EvaluationResult;
import com.mascotacare.rules.engine.entity.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * RulesEvaluator (C4 Tabla 4): carga reglas desde cache,
 * filtra por especie + edad, busca match por keyword del código de síntoma,
 * elige la de mayor prioridad (menor número) y devuelve el veredicto.
 */
@Service
@RequiredArgsConstructor
public class RulesEvaluator {

    private final RuleCacheManager cache;

    public EvaluationResult evaluate(EvaluationRequest req) {
        List<Rule> aplicables = cache.getActiveRules().stream()
                .filter(r -> r.getEspecieAplica() == Rule.EspecieAplica.TODAS
                        || r.getEspecieAplica() == req.especie())
                .filter(r -> req.edadMeses() >= r.getEdadMinMeses()
                        && req.edadMeses() <= r.getEdadMaxMeses())
                .filter(r -> matches(r.getCondicionSintoma(), req.codigosSintomas()))
                .toList();

        if (aplicables.isEmpty()) {
            return new EvaluationResult(
                    Rule.NivelUrgencia.MEDIA, null,
                    "No se encontró una regla específica. Se recomienda consulta veterinaria en 24-48h.",
                    0.4, List.of());
        }

        Rule winner = aplicables.get(0);
        List<UUID> all = aplicables.stream().map(Rule::getId).toList();
        return new EvaluationResult(
                winner.getNivelUrgenciaResultado(), winner.getId(),
                winner.getAccionRecomendada(),
                Math.min(0.6 + aplicables.size() * 0.1, 0.95),
                all);
    }

    private boolean matches(String condicion, List<String> codigos) {
        String[] keywords = condicion.toLowerCase().split("[_\\s,]+");
        for (String code : codigos) {
            String c = code.toLowerCase();
            for (String k : keywords) {
                if (k.length() > 2 && c.contains(k)) return true;
            }
        }
        return false;
    }
}
