package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cliente del LLM/AI API (componente §C4-Guide).
 * Cuando exista AI_API_KEY se conectará a OpenAI. Por ahora devuelve enriquecimiento dummy.
 */
@Slf4j
@Component
public class AIContentProvider {

    @Value("${ai.api.key:}")
    private String apiKey;

    public String enrich(String baseHtml, GuideRequest req) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("AI deshabilitado (sin AI_API_KEY) — devolviendo baseline");
            return baseHtml;
        }
        // TODO: implementar llamada real al LLM cuando esté la API key.
        // String prompt = "Enriquece esta guía veterinaria para " + req.especie() + ": " + baseHtml;
        // return openAiClient.complete(prompt);
        log.warn("AI_API_KEY presente pero cliente real aún no implementado");
        return baseHtml;
    }
}
