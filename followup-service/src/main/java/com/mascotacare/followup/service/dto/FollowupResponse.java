package com.mascotacare.followup.service.dto;

import com.mascotacare.followup.service.entity.Followup;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FollowupResponse(
        UUID id,
        UUID idConsulta,
        UUID idMascota,
        Followup.Estado estado,
        String observaciones,
        Boolean alertaEnviada,
        OffsetDateTime fechaSeguimiento
) {}
