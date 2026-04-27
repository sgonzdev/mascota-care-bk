package com.mascotacare.metrics.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MetricsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetricsServiceApplication.class, args);
    }
}
