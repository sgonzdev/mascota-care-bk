package com.mascotacare.api.gateway.audit.service;

import com.mascotacare.api.gateway.audit.entity.AuditEntry;
import com.mascotacare.api.gateway.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Casos de uso de auditoría (RF26). Escritura asíncrona para no bloquear el
 * thread de la request HTTP. Consulta paginada con filtros opcionales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repository;

    /** Persiste una entrada fire-and-forget — los errores se logean. */
    @Async
    @Transactional
    public void record(AuditEntry entry) {
        try {
            repository.save(entry);
        } catch (Exception e) {
            log.warn("audit: no se pudo persistir entrada {}: {}", entry.getAccion(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> search(UUID idUsuario, String accion,
                                    OffsetDateTime desde, OffsetDateTime hasta,
                                    Pageable pageable) {
        return repository.search(idUsuario, blankAsNull(accion), desde, hasta, pageable);
    }

    private static String blankAsNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
