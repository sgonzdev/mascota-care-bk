package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.entity.Guide;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Genera plantillas estáticas — el AIContentProvider las puede enriquecer. */
@Component
public class GuideTemplateEngine {

    private static final Map<Guide.TipoGuia, String> BASE = Map.ofEntries(
            Map.entry(Guide.TipoGuia.CUIDADO,
                    "<h2>Cuidados básicos para %s (%s)</h2>"
                    + "<ul><li>Vacunación al día.</li><li>Desparasitación según edad.</li>"
                    + "<li>Higiene dental semanal.</li><li>Ejercicio diario adecuado a la raza.</li></ul>"),

            Map.entry(Guide.TipoGuia.ALIMENTACION,
                    "<h2>Alimentación recomendada para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Ración 2 veces al día con pienso de calidad. "
                    + "Agua fresca siempre disponible. Evitar restos de comida humana.</p>"),

            Map.entry(Guide.TipoGuia.ALARMA,
                    "<h2>Señales de alarma a vigilar en %s (%s)</h2>"
                    + "<ul><li>Letargo persistente más de 24h.</li><li>Vómitos repetidos.</li>"
                    + "<li>Diarrea con sangre.</li><li>Dificultad respiratoria.</li>"
                    + "<li>Convulsiones.</li></ul>"),

            Map.entry(Guide.TipoGuia.HIGIENE,
                    "<h2>Rutina de higiene para %s (%s)</h2>"
                    + "<ul><li>Cepillado del pelaje.</li><li>Baño según especie y raza.</li>"
                    + "<li>Limpieza de oídos.</li><li>Corte de uñas.</li>"
                    + "<li>Higiene dental.</li></ul>"),

            Map.entry(Guide.TipoGuia.EJERCICIO,
                    "<h2>Plan de ejercicio para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Duración, intensidad y tipo recomendado "
                    + "según la raza y nivel de energía.</p>"),

            Map.entry(Guide.TipoGuia.VACUNACION,
                    "<h2>Calendario de vacunación para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Vacunas core y opcionales, refuerzos anuales "
                    + "y desparasitación interna/externa.</p>"),

            Map.entry(Guide.TipoGuia.COMPORTAMIENTO,
                    "<h2>Pautas de comportamiento para %s (%s)</h2>"
                    + "<ul><li>Socialización temprana.</li><li>Refuerzo positivo.</li>"
                    + "<li>Manejo de ansiedad por separación.</li>"
                    + "<li>Señales básicas (sentado, quieto, llamada).</li></ul>"),

            Map.entry(Guide.TipoGuia.CACHORRO,
                    "<h2>Cuidados de cachorro para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Adaptación al hogar, vacunación inicial, "
                    + "destete, primera socialización y aprendizaje básico.</p>"),

            Map.entry(Guide.TipoGuia.ADULTO_MAYOR,
                    "<h2>Cuidados de mascota mayor para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Revisiones semestrales, alimentación senior, "
                    + "ejercicio moderado y control de articulaciones/dentadura.</p>"),

            Map.entry(Guide.TipoGuia.VIAJE,
                    "<h2>Preparación para viaje con %s (%s)</h2>"
                    + "<ul><li>Documentación y vacunas vigentes.</li>"
                    + "<li>Transportín seguro y bien ventilado.</li>"
                    + "<li>Hidratación y paradas frecuentes.</li>"
                    + "<li>Adaptación previa al transporte.</li></ul>"),

            Map.entry(Guide.TipoGuia.PRIMEROS_AUXILIOS,
                    "<h2>Primeros auxilios para %s (%s)</h2>"
                    + "<ul><li>Heridas leves: limpieza con suero, gasa estéril.</li>"
                    + "<li>Atragantamiento: maniobra adaptada al tamaño.</li>"
                    + "<li>Golpe de calor: enfriar gradualmente.</li>"
                    + "<li>Acudir al veterinario sin demora ante dudas.</li></ul>")
    );

    public String render(GuideRequest req) {
        String template = BASE.getOrDefault(req.tipo(), "<p>Guía no disponible.</p>");
        return template.formatted(req.especie(), req.raza(), req.edadMeses());
    }
}
