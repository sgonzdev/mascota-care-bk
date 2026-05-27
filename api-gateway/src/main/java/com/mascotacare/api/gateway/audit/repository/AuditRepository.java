package com.mascotacare.api.gateway.audit.repository;

import com.mascotacare.api.gateway.audit.entity.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {

    /**
     * Búsqueda flexible — todos los filtros son opcionales. Postgres no infiere
     * el tipo de un parámetro NULL en `(:p is null or ...)`, así que usamos un
     * cast explícito y nativeQuery. El cast permite que PostgreSQL conozca el
     * tipo aunque el valor sea null.
     */
    @Query(value = "select * from audit_log a where "
            + "(cast(:idUsuario as uuid) is null or a.id_usuario = cast(:idUsuario as uuid)) and "
            + "(cast(:accion    as text) is null or a.accion    = :accion) and "
            + "(cast(:desde     as timestamptz) is null or a.creado_en >= cast(:desde as timestamptz)) and "
            + "(cast(:hasta     as timestamptz) is null or a.creado_en <= cast(:hasta as timestamptz)) "
            + "order by a.creado_en desc",
           countQuery = "select count(*) from audit_log a where "
            + "(cast(:idUsuario as uuid) is null or a.id_usuario = cast(:idUsuario as uuid)) and "
            + "(cast(:accion    as text) is null or a.accion    = :accion) and "
            + "(cast(:desde     as timestamptz) is null or a.creado_en >= cast(:desde as timestamptz)) and "
            + "(cast(:hasta     as timestamptz) is null or a.creado_en <= cast(:hasta as timestamptz))",
           nativeQuery = true)
    Page<AuditEntry> search(
            @Param("idUsuario") UUID idUsuario,
            @Param("accion") String accion,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            Pageable pageable);
}
