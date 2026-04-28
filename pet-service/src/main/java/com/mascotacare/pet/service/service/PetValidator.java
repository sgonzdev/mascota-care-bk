package com.mascotacare.pet.service.service;

import com.mascotacare.pet.service.dto.PetRequest;
import com.mascotacare.pet.service.entity.Pet.Especie;
import org.springframework.stereotype.Component;

/**
 * PetValidator (componente §C4 Pet Service): validaciones de negocio
 * que van más allá de Bean Validation: rangos de peso por especie,
 * coherencia edad/peso, etc.
 */
@Component
public class PetValidator {

    public void validate(PetRequest req) {
        validateWeightForSpecies(req.especie(), req.pesoKg());
        validateAgeWeightCoherence(req.edadMeses(), req.pesoKg(), req.especie());
    }

    private void validateWeightForSpecies(Especie especie, Double pesoKg) {
        double max = switch (especie) {
            case PERRO -> 100.0;
            case GATO  -> 15.0;
            case OTRO  -> 200.0;
        };
        if (pesoKg > max) {
            throw new IllegalArgumentException(
                    "Peso %s kg excede el máximo razonable para %s (%s kg)".formatted(pesoKg, especie, max));
        }
    }

    private void validateAgeWeightCoherence(Integer edadMeses, Double pesoKg, Especie especie) {
        if (edadMeses < 2 && pesoKg > 5 && especie == Especie.PERRO) {
            throw new IllegalArgumentException(
                    "Cachorro de %d meses con %s kg: revisar datos".formatted(edadMeses, pesoKg));
        }
        if (especie == Especie.GATO && pesoKg > 12) {
            throw new IllegalArgumentException(
                    "Peso %s kg inusualmente alto para gato".formatted(pesoKg));
        }
    }
}
