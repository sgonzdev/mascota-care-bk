package com.mascotacare.guide.service.dto;

import com.mascotacare.guide.service.entity.Guide;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record GuideRequest(
        UUID idMascota,
        @NotNull Guide.TipoGuia tipo,
        @NotBlank @Size(min = 2, max = 20) String especie,
        @NotBlank @Size(max = 80) String raza,
        @NotNull @Min(0) Integer edadMeses,
        String contextoAdicional
) {}
