package com.mascotacare.followup.service.repository;

import com.mascotacare.followup.service.entity.Followup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowupRepository extends JpaRepository<Followup, UUID> {
    Page<Followup> findByIdMascotaOrderByFechaSeguimientoDesc(UUID idMascota, Pageable pageable);
    List<Followup> findByIdConsultaOrderByFechaSeguimientoDesc(UUID idConsulta);
    List<Followup> findByEstadoAndAlertaEnviadaFalseAndFechaSeguimientoBefore(
            Followup.Estado estado, OffsetDateTime threshold);
    /** Para el scheduler RF27: ¿esta consulta ya tiene algún seguimiento? */
    boolean existsByIdConsulta(UUID idConsulta);
}
