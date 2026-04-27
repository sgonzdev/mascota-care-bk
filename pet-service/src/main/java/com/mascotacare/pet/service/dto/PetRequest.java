package com.mascotacare.pet.service.dto;

import com.mascotacare.pet.service.entity.Pet;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record PetRequest(
        @NotNull UUID idUsuario,
        @NotBlank @Size(min = 2, max = 80) String nombre,
        @NotNull Pet.Especie especie,
        @NotBlank @Size(max = 80) String raza,
        @NotNull @Min(0) @Max(360) Integer edadMeses,
        @NotNull @DecimalMin("0.1") @DecimalMax("200.0") Double pesoKg,
        @NotNull Pet.Sexo sexo
) {}
