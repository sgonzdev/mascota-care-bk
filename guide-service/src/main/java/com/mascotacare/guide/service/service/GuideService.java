package com.mascotacare.guide.service.service;

import com.mascotacare.guide.service.dto.GuideRequest;
import com.mascotacare.guide.service.dto.GuideResponse;
import com.mascotacare.guide.service.entity.Guide;
import com.mascotacare.guide.service.repository.GuideRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        String content = ai.enrich(baseline, req);
        Guide.Fuente fuente = (aiKey == null || aiKey.isBlank()) ? Guide.Fuente.TEMPLATE : Guide.Fuente.AI;
        Guide saved = repository.save(Guide.builder()
                .idMascota(req.idMascota())
                .tipo(req.tipo())
                .contenidoHtml(content)
                .fuente(fuente)
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GuideResponse> historyOf(UUID idMascota) {
        return repository.findByIdMascotaOrderByFechaGeneracionDesc(idMascota).stream()
                .map(this::toResponse).toList();
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
