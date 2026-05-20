package com.mascotacare.notification.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Anuncio GLOBAL — solo admin. */
public record BroadcastRequest(
        @Size(max = 200) String asunto,
        @NotBlank @Size(max = 5000) String contenido
) {}
