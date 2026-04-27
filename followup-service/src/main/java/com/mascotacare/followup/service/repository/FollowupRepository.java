package com.mascotacare.followup.service.repository;

import com.mascotacare.followup.service.entity.Followup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowupRepository extends JpaRepository<Followup, UUID> {
    List<Followup> findByIdMascotaOrderByFechaSeguimientoDesc(UUID idMascota);
    List<Followup> findByEstadoAndAlertaEnviadaFalseAndFechaSeguimientoBefore(
            Followup.Estado estado, OffsetDateTime threshold);
}
