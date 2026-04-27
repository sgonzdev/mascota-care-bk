package com.mascotacare.pet.service.dto;

import com.mascotacare.pet.service.entity.Pet;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PetResponse(
        UUID id,
        UUID idUsuario,
        String nombre,
        Pet.Especie especie,
        String raza,
        Integer edadMeses,
        Double pesoKg,
        Pet.Sexo sexo,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn
) {}
