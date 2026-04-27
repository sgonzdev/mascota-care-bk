package com.mascotacare.guide.service.dto;

import com.mascotacare.guide.service.entity.Guide;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GuideResponse(
        UUID id,
        UUID idMascota,
        Guide.TipoGuia tipo,
        String contenidoHtml,
        Guide.Fuente fuente,
        OffsetDateTime fechaGeneracion
) {}
