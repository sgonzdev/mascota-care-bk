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
    // mistral-small-latest: ~2-3s, calidad suficiente para guías informativas.
    // mistral-large era overkill (10-25s) sin ganancia real para este caso.
    private static final String MODEL = "mistral-small-latest";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

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
                            "max_tokens", 4000))
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
                Eres un asistente veterinario que redacta guías de cuidado detalladas
                para dueños de mascotas. Devuelve EXCLUSIVAMENTE HTML semántico
                (h2, h3, ul, ol, p, strong) en español, sin envoltorios markdown,
                sin bloques ```html ni ``` de ningún tipo, sin texto antes ni después.
                Estructura cada guía con varias secciones (al menos 4), explicaciones
                prácticas y ejemplos concretos adaptados a la especie, raza y edad
                indicadas. Evita diagnósticos. Cierra recordando que ante dudas o
                emergencias el dueño debe consultar al veterinario.
                """;
    }

    private String buildPrompt(GuideRequest req, String baseHtml) {
        return """
                Mascota: %s, raza %s, %d meses de edad.
                Tipo de guía: %s.

                Plantilla base (úsala como punto de partida, expándela):
                %s

                Genera una guía completa y útil, con al menos 4 secciones (h2/h3),
                listas con varios ítems prácticos y explicaciones claras. Personaliza
                cada consejo para esta mascota específica considerando especie, raza,
                edad y tipo de guía solicitado. Devuelve sólo HTML, sin envoltorio
                <html> ni <body>.
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
