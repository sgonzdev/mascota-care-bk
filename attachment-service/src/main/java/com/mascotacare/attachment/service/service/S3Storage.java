package com.mascotacare.attachment.service.service;

import com.mascotacare.attachment.service.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Capa de acceso a MinIO/S3. Encapsula put/get/delete sobre objetos.
 * El controlador y service de negocio no conocen el SDK directamente.
 */
@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3;
    private final S3Config cfg;

    public void upload(String key, byte[] bytes, String contentType) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(cfg.getBucket())
                        .key(key)
                        .contentType(contentType)
                        .contentLength((long) bytes.length)
                        .build(),
                RequestBody.fromBytes(bytes));
    }

    /** Devuelve un stream del objeto. El caller debe cerrarlo (try-with-resources). */
    public ResponseInputStream<GetObjectResponse> download(String key) {
        return s3.getObject(GetObjectRequest.builder()
                .bucket(cfg.getBucket())
                .key(key)
                .build());
    }

    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(cfg.getBucket())
                .key(key)
                .build());
    }

    /** Lee todo el contenido de un InputStream en memoria. Útil para downloads pequeños. */
    public static byte[] readAll(InputStream in) throws IOException {
        return in.readAllBytes();
    }
}
