package com.mascotacare.symptom.service.repository;

import com.mascotacare.symptom.service.entity.Symptom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {
    Page<Symptom> findByIdMascotaOrderByFechaReporteDesc(UUID idMascota, Pageable pageable);
}
