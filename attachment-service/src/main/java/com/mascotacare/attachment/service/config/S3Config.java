package com.mascotacare.attachment.service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Configura un cliente S3 apuntando a MinIO. El cliente es thread-safe y se
 * comparte entre todas las peticiones (singleton de Spring).
 */
@Configuration
@Getter
public class S3Config {

    @Value("${mascotacare.s3.endpoint}")    private String endpoint;
    @Value("${mascotacare.s3.region}")      private String region;
    @Value("${mascotacare.s3.bucket}")      private String bucket;
    @Value("${mascotacare.s3.access-key}")  private String accessKey;
    @Value("${mascotacare.s3.secret-key}")  private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // MinIO requiere path-style; los buckets virtual-host fallan.
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true).build())
                .build();
    }
}
