package com.mascotacare.rules.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Rule implements Serializable {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "condicion_sintoma", nullable = false, length = 200)
    private String condicionSintoma;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie_aplica", nullable = false, length = 10)
    private EspecieAplica especieAplica;

    @Column(name = "edad_min_meses", nullable = false)
    private Integer edadMinMeses;

    @Column(name = "edad_max_meses", nullable = false)
    private Integer edadMaxMeses;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_urgencia_resultado", nullable = false, length = 10)
    private NivelUrgencia nivelUrgenciaResultado;

    @Column(name = "accion_recomendada", nullable = false, columnDefinition = "TEXT")
    private String accionRecomendada;

    @Column(nullable = false)
    private Integer prioridad;

    @Column(nullable = false)
    private Boolean activa;

    @CreationTimestamp
    @Column(name = "creada_en", updatable = false)
    private OffsetDateTime creadaEn;

    @UpdateTimestamp
    @Column(name = "actualizada_en")
    private OffsetDateTime actualizadaEn;

    public enum NivelUrgencia { ALTA, MEDIA, BAJA }
    public enum EspecieAplica { PERRO, GATO, OTRO, TODAS }
}
