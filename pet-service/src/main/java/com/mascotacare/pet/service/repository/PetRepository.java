package com.mascotacare.pet.service.repository;

import com.mascotacare.pet.service.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {
    List<Pet> findByIdUsuario(UUID idUsuario);
}
