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
    @Column(nullable = false, length = 32)
    private TipoGuia tipo;

    @Column(name = "contenido_html", nullable = false, columnDefinition = "TEXT")
    private String contenidoHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Fuente fuente;

    @CreationTimestamp
    @Column(name = "fecha_generacion", updatable = false)
    private OffsetDateTime fechaGeneracion;

    public enum TipoGuia {
        // Existentes
        CUIDADO, ALIMENTACION, ALARMA,
        HIGIENE, EJERCICIO, VACUNACION, COMPORTAMIENTO,
        CACHORRO, ADULTO_MAYOR, VIAJE, PRIMEROS_AUXILIOS,
        // Nuevas — temas habituales en consulta veterinaria primaria
        SOCIALIZACION,        // socialización temprana, miedos, exposición controlada
        ADIESTRAMIENTO,       // órdenes básicas, refuerzo positivo
        ESTERILIZACION,       // castración/esterilización: cuándo, beneficios, postoperatorio
        REPRODUCCION,         // celo, gestación, parto, lactancia
        PARASITOS,            // pulgas, garrapatas, gusanos: prevención y tratamiento
        DENTAL,               // higiene dental, sarro, enfermedad periodontal
        OBESIDAD,             // control de peso, dieta, ejercicio en sobrepeso
        DERMATOLOGIA,         // problemas de piel, alergias, dermatitis
        ANSIEDAD_SEPARACION,  // manejo de ansiedad por separación
        ENRIQUECIMIENTO,      // estimulación mental, juguetes, ambiente
        PRIMER_ANIO,          // hitos del primer año de vida
        EMERGENCIAS_HOGAR     // qué tener en casa, kit veterinario, intoxicaciones
    }
    public enum Fuente { TEMPLATE, AI }
}
