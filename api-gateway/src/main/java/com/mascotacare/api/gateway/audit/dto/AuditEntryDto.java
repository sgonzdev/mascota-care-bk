package com.mascotacare.api.gateway.audit.dto;

import com.mascotacare.api.gateway.audit.entity.AuditEntry;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Vista plana de una entrada de auditoría para la API. */
public record AuditEntryDto(
        UUID id,
        UUID idUsuario,
        String email,
        String rol,
        String accion,
        String metodo,
        String path,
        Integer statusCode,
        String ip,
        String userAgent,
        String detalle,
        OffsetDateTime creadoEn) {

    public static AuditEntryDto of(AuditEntry e) {
        return new AuditEntryDto(
                e.getId(), e.getIdUsuario(), e.getEmail(), e.getRol(),
                e.getAccion(), e.getMetodo(), e.getPath(), e.getStatusCode(),
                e.getIp(), e.getUserAgent(), e.getDetalle(), e.getCreadoEn());
    }
}
