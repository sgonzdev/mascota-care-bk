package com.mascotacare.guide.service.repository;

import com.mascotacare.guide.service.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideRepository extends JpaRepository<Guide, UUID> {
    List<Guide> findByIdMascotaOrderByFechaGeneracionDesc(UUID idMascota);
}
