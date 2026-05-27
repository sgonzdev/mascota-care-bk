package com.mascotacare.attachment.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Attachment {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "id_consulta", nullable = false)
    private UUID idConsulta;

    /** Usuario que subió el archivo (dueño, vet asignado o admin). */
    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Ruta del objeto en MinIO: {idConsulta}/{uuid}-{nombreSanitizado}. */
    @Column(name = "s3_key", nullable = false, unique = true, length = 500)
    private String s3Key;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;
}
