package com.mascotacare.symptom.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "symptoms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Symptom {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_mascota", nullable = false)
    private UUID idMascota;

    @Column(name = "descripcion_libre", nullable = false, columnDefinition = "TEXT")
    private String descripcionLibre;

    @Column(name = "codigos_normalizados", nullable = false, length = 500)
    private String codigosNormalizados;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad_percibida", length = 10)
    private Severidad severidadPercibida;

    @CreationTimestamp
    @Column(name = "fecha_reporte", nullable = false, updatable = false)
    private OffsetDateTime fechaReporte;

    public enum Severidad { LEVE, MODERADA, SEVERA }
}
