package com.mascotacare.rules.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class RulesEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RulesEngineApplication.class, args);
    }
}
