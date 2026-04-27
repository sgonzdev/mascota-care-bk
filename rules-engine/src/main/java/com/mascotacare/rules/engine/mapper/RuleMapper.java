package com.mascotacare.rules.engine.mapper;

import com.mascotacare.rules.engine.dto.RuleRequest;
import com.mascotacare.rules.engine.dto.RuleResponse;
import com.mascotacare.rules.engine.entity.Rule;
import org.springframework.stereotype.Component;

@Component
public class RuleMapper {

    public Rule toEntity(RuleRequest req) {
        return Rule.builder()
                .condicionSintoma(req.condicionSintoma())
                .especieAplica(req.especieAplica())
                .edadMinMeses(req.edadMinMeses())
                .edadMaxMeses(req.edadMaxMeses())
                .nivelUrgenciaResultado(req.nivelUrgenciaResultado())
                .accionRecomendada(req.accionRecomendada())
                .prioridad(req.prioridad())
                .activa(req.activa())
                .build();
    }

    public void update(Rule entity, RuleRequest req) {
        entity.setCondicionSintoma(req.condicionSintoma());
        entity.setEspecieAplica(req.especieAplica());
        entity.setEdadMinMeses(req.edadMinMeses());
        entity.setEdadMaxMeses(req.edadMaxMeses());
        entity.setNivelUrgenciaResultado(req.nivelUrgenciaResultado());
        entity.setAccionRecomendada(req.accionRecomendada());
        entity.setPrioridad(req.prioridad());
        entity.setActiva(req.activa());
    }

    public RuleResponse toResponse(Rule r) {
        return new RuleResponse(
                r.getId(), r.getCondicionSintoma(), r.getEspecieAplica(),
                r.getEdadMinMeses(), r.getEdadMaxMeses(),
                r.getNivelUrgenciaResultado(), r.getAccionRecomendada(),
                r.getPrioridad(), r.getActiva(),
                r.getCreadaEn(), r.getActualizadaEn());
    }
}
