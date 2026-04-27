package com.mascotacare.pet.service.mapper;

import com.mascotacare.pet.service.dto.PetRequest;
import com.mascotacare.pet.service.dto.PetResponse;
import com.mascotacare.pet.service.entity.Pet;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(PetRequest req) {
        return Pet.builder()
                .idUsuario(req.idUsuario())
                .nombre(req.nombre())
                .especie(req.especie())
                .raza(req.raza())
                .edadMeses(req.edadMeses())
                .pesoKg(req.pesoKg())
                .sexo(req.sexo())
                .build();
    }

    public void update(Pet entity, PetRequest req) {
        entity.setNombre(req.nombre());
        entity.setEspecie(req.especie());
        entity.setRaza(req.raza());
        entity.setEdadMeses(req.edadMeses());
        entity.setPesoKg(req.pesoKg());
        entity.setSexo(req.sexo());
    }

    public PetResponse toResponse(Pet p) {
        return new PetResponse(
                p.getId(), p.getIdUsuario(), p.getNombre(),
                p.getEspecie(), p.getRaza(), p.getEdadMeses(),
                p.getPesoKg(), p.getSexo(),
                p.getCreadoEn(), p.getActualizadoEn()
        );
    }
}
