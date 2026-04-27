package com.mascotacare.followup.service.dto;

import com.mascotacare.followup.service.entity.Followup;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record FollowupRequest(
        @NotNull UUID idConsulta,
        @NotNull UUID idMascota,
        @NotNull Followup.Estado estado,
        @Size(max = 2000) String observaciones
) {}
