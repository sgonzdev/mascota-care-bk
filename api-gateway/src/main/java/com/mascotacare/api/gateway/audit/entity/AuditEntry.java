package com.mascotacare.api.gateway.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Entrada de auditoría persistida (RF26). Trazabilidad de acciones críticas. */
@Entity
@Table(name = "audit_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEntry {

    @Id @GeneratedValue
    private UUID id;

    /** Null si la acción es del sistema (sin sesión asociada). */
    @Column(name = "id_usuario")
    private UUID idUsuario;

    @Column(length = 200)
    private String email;

    @Column(length = 20)
    private String rol;

    /** Código de acción legible: AUTH_LOGIN, RULE_TOGGLE, CONSULTA_CLAIM, ... */
    @Column(nullable = false, length = 80)
    private String accion;

    @Column(length = 10)
    private String metodo;

    @Column(length = 500)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;
}
