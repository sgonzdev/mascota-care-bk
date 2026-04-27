package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.entity.Guide;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Genera plantillas estáticas — el AIContentProvider las puede enriquecer. */
@Component
public class GuideTemplateEngine {

    private static final Map<Guide.TipoGuia, String> BASE = Map.of(
            Guide.TipoGuia.CUIDADO,
            "<h2>Cuidados básicos para %s (%s)</h2>"
            + "<ul><li>Vacunación al día.</li><li>Desparasitación según edad.</li>"
            + "<li>Higiene dental semanal.</li><li>Ejercicio diario adecuado a la raza.</li></ul>",

            Guide.TipoGuia.ALIMENTACION,
            "<h2>Alimentación recomendada para %s (%s)</h2>"
            + "<p>Edad: %d meses. Ración 2 veces al día con pienso de calidad. "
            + "Agua fresca siempre disponible. Evitar restos de comida humana.</p>",

            Guide.TipoGuia.ALARMA,
            "<h2>Señales de alarma a vigilar en %s (%s)</h2>"
            + "<ul><li>Letargo persistente más de 24h.</li><li>Vómitos repetidos.</li>"
            + "<li>Diarrea con sangre.</li><li>Dificultad respiratoria.</li>"
            + "<li>Convulsiones.</li></ul>"
    );

    public String render(GuideRequest req) {
        String template = BASE.getOrDefault(req.tipo(), "<p>Guía no disponible.</p>");
        return template.formatted(req.especie(), req.raza(), req.edadMeses());
    }
}
