package com.mascotacare.notification.service.dto;

import com.mascotacare.notification.service.entity.Notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String destinatario,
        Notification.Canal canal,
        String asunto,
        String contenido,
        Notification.Estado estado,
        Integer intentos,
        String errorMessage,
        OffsetDateTime creadaEn,
        OffsetDateTime enviadaEn
) {}
