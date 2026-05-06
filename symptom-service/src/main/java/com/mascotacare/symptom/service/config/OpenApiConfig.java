package com.mascotacare.symptom.service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MascotaCare — symptom-service",
                version = "1.0.0",
                description = "Microservicio del sistema MascotaCare Soluciona. "
                        + "Todas las llamadas requieren JWT en cabecera Authorization "
                        + "(excepto /actuator y /v3/api-docs).",
                contact = @Contact(name = "Santiago Gonzalez · Jhon Esteban Pinto"),
                license = @License(name = "Académico", url = "https://github.com/")
        ),
        security = { @SecurityRequirement(name = "bearer-jwt") }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega aquí el accessToken obtenido en POST /auth/login. "
                + "NO añadas la palabra 'Bearer ', Swagger la añade automáticamente."
)
public class OpenApiConfig {}
