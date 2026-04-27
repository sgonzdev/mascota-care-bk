package com.mascotacare.guide.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "guides")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Guide {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_mascota")
    private UUID idMascota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoGuia tipo;

    @Column(name = "contenido_html", nullable = false, columnDefinition = "TEXT")
    private String contenidoHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Fuente fuente;

    @CreationTimestamp
    @Column(name = "fecha_generacion", updatable = false)
    private OffsetDateTime fechaGeneracion;

    public enum TipoGuia { CUIDADO, ALIMENTACION, ALARMA }
    public enum Fuente { TEMPLATE, AI }
}
