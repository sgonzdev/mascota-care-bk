package com.mascotacare.followup.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "followups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Followup {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_consulta", nullable = false)
    private UUID idConsulta;

    @Column(name = "id_mascota", nullable = false)
    private UUID idMascota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Estado estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "alerta_enviada", nullable = false)
    private Boolean alertaEnviada;

    @CreationTimestamp
    @Column(name = "fecha_seguimiento", updatable = false)
    private OffsetDateTime fechaSeguimiento;

    public enum Estado { MEJORO, NO_MEJORO, SIN_DATO }
}
