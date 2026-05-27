package com.mascotacare.attachment.service.service;

import com.mascotacare.attachment.service.dto.AttachmentResponse;
import com.mascotacare.attachment.service.entity.Attachment;
import com.mascotacare.attachment.service.repository.AttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso de adjuntos. Orquesta persistencia de metadata + binario en S3.
 * Mantiene el binario y la fila consistentes con compensación en caso de error.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository repository;
    private final S3Storage storage;

    public AttachmentResponse upload(UUID idConsulta, UUID idUsuario, MultipartFile file)
            throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Archivo vacío");
        String safeName = sanitize(file.getOriginalFilename());
        String key = "%s/%s-%s".formatted(idConsulta, UUID.randomUUID(), safeName);
        String contentType = file.getContentType() != null
                ? file.getContentType() : "application/octet-stream";

        storage.upload(key, file.getBytes(), contentType);
        try {
            Attachment saved = repository.save(Attachment.builder()
                    .idConsulta(idConsulta)
                    .idUsuario(idUsuario)
                    .nombreArchivo(safeName)
                    .mimeType(contentType)
                    .sizeBytes(file.getSize())
                    .s3Key(key)
                    .build());
            return AttachmentResponse.from(saved);
        } catch (RuntimeException e) {
            // Compensación: si falla la persistencia, borramos el objeto subido
            // para no dejar huérfanos en S3.
            try { storage.delete(key); } catch (Exception ignore) {}
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listByConsulta(UUID idConsulta) {
        return repository.findByIdConsultaOrderByCreadoEnDesc(idConsulta)
                .stream().map(AttachmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Attachment findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto " + id + " no encontrado"));
    }

    public void delete(UUID id, UUID requesterId, boolean isAdmin) {
        Attachment a = findEntity(id);
        if (!isAdmin && !a.getIdUsuario().equals(requesterId)) {
            throw new IllegalStateException("No tienes permiso para borrar este adjunto");
        }
        storage.delete(a.getS3Key());
        repository.delete(a);
    }

    /** Quita separadores de ruta del nombre original. Mantiene letras, dígitos, dot, dash, underscore. */
    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "archivo";
        String base = Paths.get(name).getFileName().toString();
        String cleaned = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.length() > 200 ? cleaned.substring(cleaned.length() - 200) : cleaned;
    }
}
