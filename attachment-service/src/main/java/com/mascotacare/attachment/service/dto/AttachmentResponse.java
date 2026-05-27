package com.mascotacare.attachment.service.dto;

import com.mascotacare.attachment.service.entity.Attachment;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID idConsulta,
        UUID idUsuario,
        String nombreArchivo,
        String mimeType,
        long sizeBytes,
        OffsetDateTime creadoEn
) {
    public static AttachmentResponse from(Attachment a) {
        return new AttachmentResponse(
                a.getId(), a.getIdConsulta(), a.getIdUsuario(),
                a.getNombreArchivo(), a.getMimeType(), a.getSizeBytes(),
                a.getCreadoEn());
    }
}
