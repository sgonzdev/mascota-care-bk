package com.mascotacare.notification.service.dto;

import com.mascotacare.notification.service.entity.Notification;
import jakarta.validation.constraints.*;

public record NotificationRequest(
        @NotBlank @Size(max = 200) String destinatario,
        @NotNull Notification.Canal canal,
        @Size(max = 200) String asunto,
        @NotBlank @Size(max = 5000) String contenido
) {}
