package com.mascotacare.symptom.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Agregación persistida por cada triage UC2+UC3.
 * Expuesta al frontend mediante /api/consultations.
 */
@Entity
@Table(name = "consultas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Consulta {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_mascota", nullable = false)
    private UUID idMascota;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private OffsetDateTime fechaHora;

    @Column(name = "descripcion_sintomas", nullable = false, columnDefinition = "TEXT")
    private String descripcionSintomas;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_urgencia", nullable = false, length = 10)
    private NivelUrgencia nivelUrgencia;

    @Column(name = "respuesta_generada", nullable = false, columnDefinition = "TEXT")
    private String respuestaGenerada;

    @Column(name = "id_regla_aplicada")
    private UUID idReglaAplicada;

    @Column(name = "canal", nullable = false, length = 10)
    @Builder.Default
    private String canal = "web";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private EstadoConsulta estado = EstadoConsulta.activa;

    @Column(name = "notas_internas", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String notasInternas = "";

    @UpdateTimestamp
    @Column(name = "actualizada_en", nullable = false)
    private OffsetDateTime actualizadaEn;

    /** Veterinario que ha tomado el caso (claim exclusivo) — RF22. */
    @Column(name = "id_vet_asignado")
    private UUID idVetAsignado;

    /** Snapshot del nombre del vet al momento del claim — evita llamadas al auth-service. */
    @Column(name = "nombre_vet_asignado", length = 80)
    private String nombreVetAsignado;

    /** Email del vet (para que el dueño pueda contactarlo). */
    @Column(name = "email_vet_asignado", length = 200)
    private String emailVetAsignado;

    /** Teléfono del vet (para contacto). */
    @Column(name = "telefono_vet_asignado", length = 20)
    private String telefonoVetAsignado;

    @Column(name = "asignada_en")
    private OffsetDateTime asignadaEn;

    public enum NivelUrgencia { ALTA, MEDIA, BAJA }
    public enum EstadoConsulta { activa, resuelta, archivada, pendiente }
}
