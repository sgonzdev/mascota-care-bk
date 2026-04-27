package com.mascotacare.pet.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pet {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Especie especie;

    @Column(nullable = false, length = 80)
    private String raza;

    @Column(name = "edad_meses", nullable = false)
    private Integer edadMeses;

    @Column(name = "peso_kg", nullable = false)
    private Double pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sexo sexo;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    public enum Especie { PERRO, GATO, OTRO }
    public enum Sexo { MACHO, HEMBRA }
}
