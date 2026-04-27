package com.mascotacare.notification.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 200)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Canal canal;

    @Column(length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Estado estado;

    @Column(name = "intentos", nullable = false)
    private Integer intentos;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "creada_en", updatable = false)
    private OffsetDateTime creadaEn;

    @Column(name = "enviada_en")
    private OffsetDateTime enviadaEn;

    public enum Canal { PUSH, EMAIL, SMS }
    public enum Estado { PENDIENTE, ENVIADA, FALLIDA }
}
