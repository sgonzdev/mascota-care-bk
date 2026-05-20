package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.dto.GuideResponse;
import com.mascotacare.guide.service.dto.PageResponse;
import com.mascotacare.guide.service.entity.Guide;
import com.mascotacare.guide.service.repository.GuideRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GuideService {

    private final GuideRepository repository;
    private final GuideTemplateEngine templateEngine;
    private final AIContentProvider ai;

    @Value("${ai.api.key:}")
    private String aiKey;

    public GuideResponse generate(GuideRequest req) {
        String baseline = templateEngine.render(req);
        String content = stripMarkdownFences(ai.enrich(baseline, req));
        Guide.Fuente fuente = (aiKey == null || aiKey.isBlank()) ? Guide.Fuente.TEMPLATE : Guide.Fuente.AI;
        Guide saved = repository.saveAndFlush(Guide.builder()
                .idMascota(req.idMascota())
                .tipo(req.tipo())
                .contenidoHtml(content)
                .fuente(fuente)
                .build());
        // saveAndFlush dispara @CreationTimestamp, pero la entidad en memoria aún
        // puede tener fechaGeneracion=null. Recargamos para garantizar la fecha.
        return toResponse(repository.findById(saved.getId()).orElse(saved));
    }

    /** Mistral a veces devuelve ```html ...``` en lugar de HTML puro. Lo desenvolvemos. */
    private static String stripMarkdownFences(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) trimmed = trimmed.substring(firstNewline + 1);
            if (trimmed.endsWith("```")) trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    @Transactional(readOnly = true)
    public PageResponse<GuideResponse> historyOf(UUID idMascota, Pageable pageable) {
        return PageResponse.from(
                repository.findByIdMascotaOrderByFechaGeneracionDesc(idMascota, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public GuideResponse findById(UUID id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guía " + id + " no encontrada")));
    }

    private GuideResponse toResponse(Guide g) {
        return new GuideResponse(
                g.getId(), g.getIdMascota(), g.getTipo(),
                g.getContenidoHtml(), g.getFuente(), g.getFechaGeneracion());
    }
}
