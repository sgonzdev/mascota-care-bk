package com.mascotacare.notification.service.clinics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Clínica veterinaria cercana a una ubicación")
public record ClinicDto(
        @Schema(description = "Identificador único de la clínica", example = "node/123456789")
        String id,
        @Schema(description = "Nombre comercial", example = "Veterinaria Centro")
        String nombre,
        @Schema(description = "Dirección legible", example = "Calle Mayor 12, Madrid")
        String direccion,
        @Schema(description = "Teléfono de contacto", example = "+34 91 555 1234", nullable = true)
        String telefono,
        @Schema(description = "Latitud de la clínica", example = "40.4168")
        double lat,
        @Schema(description = "Longitud de la clínica", example = "-3.7038")
        double lng,
        @Schema(description = "Distancia en metros desde el punto consultado", example = "850")
        long distanciaMetros,
        @Schema(description = "Proveedor de origen del dato", example = "OSM",
                allowableValues = {"GOOGLE", "OSM"})
        String fuente
) {}
