package com.mascotacare.symptom.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SymptomServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SymptomServiceApplication.class, args);
    }
}
