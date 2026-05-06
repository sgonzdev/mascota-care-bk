package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente del LLM/AI API (componente §C4-Guide).
 * Implementación con Mistral AI · https://docs.mistral.ai/api/
 * Si MISTRAL_API_KEY está vacío → devuelve la plantilla sin enriquecer.
 */
@Slf4j
@Component
public class AIContentProvider {

    private static final String MISTRAL_URL = "https://api.mistral.ai/v1/chat/completions";
    private static final String MODEL = "mistral-large-latest";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final WebClient webClient;
    private final String apiKey;

    public AIContentProvider(@Value("${ai.api.key:}") String apiKey,
                             WebClient.Builder builder) {
        this.apiKey = apiKey;
        this.webClient = builder.baseUrl(MISTRAL_URL).build();
    }

    public String enrich(String baseHtml, GuideRequest req) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("AI deshabilitado (sin MISTRAL_API_KEY) — devolviendo baseline");
            return baseHtml;
        }
        try {
            String prompt = buildPrompt(req, baseHtml);
            Map<?, ?> response = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "model", MODEL,
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt()),
                                    Map.of("role", "user", "content", prompt)),
                            "temperature", 0.4,
                            "max_tokens", 800))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();
            return extractContent(response, baseHtml);
        } catch (Exception e) {
            log.warn("Mistral falló, usando plantilla baseline: {}", e.getMessage());
            return baseHtml;
        }
    }

    private String systemPrompt() {
        return """
                Eres un asistente veterinario que enriquece guías de cuidado para dueños de mascotas.
                Devuelve SIEMPRE HTML limpio y conciso (h2, h3, ul, p), en español.
                Adapta el contenido a la especie, raza y edad. Sé práctico, no diagnostiques.
                Recuerda al final que ante dudas o emergencias, consulten al veterinario.
                """;
    }

    private String buildPrompt(GuideRequest req, String baseHtml) {
        return """
                Mascota: %s, raza %s, %d meses de edad.
                Tipo de guía: %s.
                Plantilla base actual:
                %s

                Personaliza y enriquece la plantilla anterior con consejos específicos
                para esta mascota. Mantén el formato HTML.
                """.formatted(req.especie(), req.raza(), req.edadMeses(), req.tipo(), baseHtml);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response, String fallback) {
        if (response == null) return fallback;
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return fallback;
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return fallback;
        String content = (String) message.get("content");
        return content == null || content.isBlank() ? fallback : content;
    }
}
