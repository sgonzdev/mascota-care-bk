package com.mascotacare.api.gateway.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private OffsetDateTime fechaRegistro;

    public enum Rol {
        DUENO,        // dueño de mascota (usuario final)
        ADMIN,        // administrador del sistema
        VETERINARIO   // veterinario externo: recibe alertas y atiende casos derivados
    }
}
