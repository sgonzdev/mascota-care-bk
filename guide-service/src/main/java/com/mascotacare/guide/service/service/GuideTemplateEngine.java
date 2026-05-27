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
                    + "<li>Acudir al veterinario sin demora ante dudas.</li></ul>"),

            // ===== Nuevas guías =====
            Map.entry(Guide.TipoGuia.SOCIALIZACION,
                    "<h2>Socialización para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Exposición gradual a personas, otros animales, "
                    + "sonidos y entornos nuevos. La ventana crítica de socialización en "
                    + "perros es 3-14 semanas; en gatos 2-9 semanas.</p>"),

            Map.entry(Guide.TipoGuia.ADIESTRAMIENTO,
                    "<h2>Adiestramiento básico para %s (%s)</h2>"
                    + "<ul><li>Refuerzo positivo (premios, voz amable).</li>"
                    + "<li>Sesiones cortas y frecuentes (5-10 min).</li>"
                    + "<li>Órdenes: sentado, quieto, llamada, junto.</li>"
                    + "<li>Evitar castigos físicos — generan miedo, no aprendizaje.</li></ul>"),

            Map.entry(Guide.TipoGuia.ESTERILIZACION,
                    "<h2>Esterilización / castración de %s (%s)</h2>"
                    + "<p>Edad: %d meses. Momento óptimo, beneficios (prevención de tumores, "
                    + "cambios de comportamiento), preparación pre-quirúrgica y cuidados "
                    + "postoperatorios (collar isabelino, reposo, control de la herida).</p>"),

            Map.entry(Guide.TipoGuia.REPRODUCCION,
                    "<h2>Reproducción de %s (%s)</h2>"
                    + "<p>Edad: %d meses. Identificación del celo, momento adecuado para "
                    + "la monta, signos de gestación, duración (≈63 días en perro/gato), "
                    + "preparación del parto y cuidados durante la lactancia.</p>"),

            Map.entry(Guide.TipoGuia.PARASITOS,
                    "<h2>Control de parásitos en %s (%s)</h2>"
                    + "<ul><li>Externos: pulgas, garrapatas, ácaros — pipetas y collares mensuales.</li>"
                    + "<li>Internos: gusanos redondos, planos — desparasitación oral trimestral.</li>"
                    + "<li>Revisar oídos, vientre y entre los dedos tras paseos.</li>"
                    + "<li>Ambiente: lavar camas y aspirar regularmente.</li></ul>"),

            Map.entry(Guide.TipoGuia.DENTAL,
                    "<h2>Cuidado dental para %s (%s)</h2>"
                    + "<ul><li>Cepillado 2-3 veces por semana con pasta veterinaria.</li>"
                    + "<li>Snacks dentales como complemento, no sustituto.</li>"
                    + "<li>Revisión anual: detección de sarro, gingivitis y piezas dañadas.</li>"
                    + "<li>Mal aliento persistente = signo de enfermedad periodontal.</li></ul>"),

            Map.entry(Guide.TipoGuia.OBESIDAD,
                    "<h2>Control de peso para %s (%s)</h2>"
                    + "<p>Edad: %d meses. Evaluar condición corporal (1-9 ideal 4-5). "
                    + "Reducir ración 10-20%%, eliminar premios calóricos, sustituir por "
                    + "verduras hervidas. Aumentar ejercicio gradualmente. Pesaje "
                    + "quincenal — bajada saludable 1-2%% peso/semana.</p>"),

            Map.entry(Guide.TipoGuia.DERMATOLOGIA,
                    "<h2>Salud de la piel en %s (%s)</h2>"
                    + "<ul><li>Picor persistente, enrojecimiento o caída de pelo: consultar.</li>"
                    + "<li>Causas comunes: alergia alimentaria, atopia, parásitos.</li>"
                    + "<li>No bañar en exceso — destruye barrera lipídica.</li>"
                    + "<li>Champús medicados solo bajo prescripción veterinaria.</li></ul>"),

            Map.entry(Guide.TipoGuia.ANSIEDAD_SEPARACION,
                    "<h2>Ansiedad por separación en %s (%s)</h2>"
                    + "<ul><li>Salidas y entradas tranquilas, sin grandes saludos.</li>"
                    + "<li>Acostumbrar a quedarse solo en sesiones progresivas.</li>"
                    + "<li>Juguetes interactivos (Kong, dispensadores) para entretener.</li>"
                    + "<li>Casos graves: combinar adiestramiento con apoyo veterinario.</li></ul>"),

            Map.entry(Guide.TipoGuia.ENRIQUECIMIENTO,
                    "<h2>Enriquecimiento ambiental para %s (%s)</h2>"
                    + "<ul><li>Juguetes de inteligencia y dispensadores de comida.</li>"
                    + "<li>Rotar los juguetes para evitar el aburrimiento.</li>"
                    + "<li>Paseos en lugares distintos para estimular el olfato.</li>"
                    + "<li>Tiempo de juego diario con el dueño — refuerza el vínculo.</li></ul>"),

            Map.entry(Guide.TipoGuia.PRIMER_ANIO,
                    "<h2>Hitos del primer año de %s (%s)</h2>"
                    + "<p>Edad: %d meses. 6-8 semanas: primera vacuna y desparasitación. "
                    + "2-4 meses: socialización intensa. 4-6 meses: vacunas refuerzo, "
                    + "esterilización opcional. 6-12 meses: cambio a dieta junior/adulto, "
                    + "control de crecimiento, primer chequeo anual.</p>"),

            Map.entry(Guide.TipoGuia.EMERGENCIAS_HOGAR,
                    "<h2>Kit de emergencias en casa para %s (%s)</h2>"
                    + "<ul><li>Botiquín: gasas, suero fisiológico, vendas, antiséptico apto, "
                    + "tijeras, termómetro digital, pinzas.</li>"
                    + "<li>Teléfonos: veterinario habitual, urgencias 24h y centro toxicológico.</li>"
                    + "<li>Lista de tóxicos comunes (chocolate, uvas, xilitol, cebolla, "
                    + "ibuprofeno, plantas como lirio o azaleas).</li>"
                    + "<li>Manta térmica y transportín siempre accesibles.</li></ul>")
    );

    public String render(GuideRequest req) {
        String template = BASE.getOrDefault(req.tipo(), "<p>Guía no disponible.</p>");
        return template.formatted(req.especie(), req.raza(), req.edadMeses());
    }
}
