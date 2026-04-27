package com.mascotacare.symptom.service.repository;

import com.mascotacare.symptom.service.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {
    List<Symptom> findByIdMascotaOrderByFechaReporteDesc(UUID idMascota);
}
