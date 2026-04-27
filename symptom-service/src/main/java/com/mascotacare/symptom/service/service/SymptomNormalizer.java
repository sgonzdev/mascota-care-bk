package com.mascotacare.symptom.service.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;

/**
 * Convierte texto libre del dueño a códigos canónicos de síntoma.
 * Componente §C4-Symptom: SymptomNormalizer.
 */
@Component
public class SymptomNormalizer {

    private static final Map<String, List<String>> KEYWORD_TO_CODE = Map.ofEntries(
            Map.entry("diarrea", List.of("diarrea")),
            Map.entry("sangre", List.of("sangrado")),
            Map.entry("vomito", List.of("vomito")),
            Map.entry("vomita", List.of("vomito")),
            Map.entry("letargo", List.of("letargo")),
            Map.entry("letargico", List.of("letargo")),
            Map.entry("decaido", List.of("letargo")),
            Map.entry("respirar", List.of("dificultad_respiratoria")),
            Map.entry("respira", List.of("dificultad_respiratoria")),
            Map.entry("ahoga", List.of("dificultad_respiratoria")),
            Map.entry("tos", List.of("tos")),
            Map.entry("come", List.of("anorexia")),
            Map.entry("apetito", List.of("anorexia")),
            Map.entry("piel", List.of("dermatitis")),
            Map.entry("rasca", List.of("prurito")),
            Map.entry("fiebre", List.of("fiebre")),
            Map.entry("convulsion", List.of("convulsiones")),
            Map.entry("convulsiones", List.of("convulsiones"))
    );

    public List<String> normalize(String descripcionLibre) {
        String text = stripAccents(descripcionLibre.toLowerCase());
        Set<String> codes = new LinkedHashSet<>();
        KEYWORD_TO_CODE.forEach((kw, list) -> {
            if (text.contains(kw)) codes.addAll(list);
        });
        if (codes.isEmpty()) codes.add("sintoma_no_categorizado");
        return new ArrayList<>(codes);
    }

    private static String stripAccents(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
