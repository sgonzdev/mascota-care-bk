package com.mascotacare.rules.engine.service;

import com.mascotacare.rules.engine.cache.RuleCacheManager;
import com.mascotacare.rules.engine.dto.EvaluationRequest;
import com.mascotacare.rules.engine.dto.EvaluationResult;
import com.mascotacare.rules.engine.entity.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * RulesEvaluator (C4 Tabla 4): carga reglas desde cache,
 * filtra por especie + edad, busca match por keyword del código de síntoma,
 * y aplica la regla "worst case wins" propia del triage médico/veterinario:
 *
 *   1. Si matchean varias reglas, gana la de MAYOR nivel de urgencia
 *      (ALTA > MEDIA > BAJA). Subestimar un síntoma grave por la presencia
 *      simultánea de uno leve es un riesgo clínico real.
 *   2. Dentro del mismo nivel, desempata por `prioridad` ascendente (más
 *      específica primero — menor número = más específica).
 *
 * El EvaluationResult devuelve la regla ganadora + el listado completo de
 * `reglas disparadas` para que la UI pueda mostrar contexto adicional.
 */
@Service
@RequiredArgsConstructor
public class RulesEvaluator {

    private static final Comparator<Rule> WORST_CASE_FIRST = Comparator
            .comparing((Rule r) -> severity(r.getNivelUrgenciaResultado())).reversed()
            .thenComparingInt(Rule::getPrioridad);

    private final RuleCacheManager cache;

    public EvaluationResult evaluate(EvaluationRequest req) {
        List<Rule> aplicables = cache.getActiveRules().stream()
                .filter(r -> r.getEspecieAplica() == Rule.EspecieAplica.TODAS
                        || r.getEspecieAplica() == req.especie())
                .filter(r -> req.edadMeses() >= r.getEdadMinMeses()
                        && req.edadMeses() <= r.getEdadMaxMeses())
                .filter(r -> matches(r.getCondicionSintoma(), req.codigosSintomas()))
                .sorted(WORST_CASE_FIRST)
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

    private static int severity(Rule.NivelUrgencia n) {
        return switch (n) { case ALTA -> 3; case MEDIA -> 2; case BAJA -> 1; };
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
