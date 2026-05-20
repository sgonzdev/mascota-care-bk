package com.mascotacare.symptom.service.repository;

import com.mascotacare.symptom.service.entity.Consulta;
import com.mascotacare.symptom.service.entity.Consulta.EstadoConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    Page<Consulta> findByIdUsuarioOrderByFechaHoraDesc(UUID idUsuario, Pageable pageable);
    Page<Consulta> findByIdUsuarioAndEstadoOrderByFechaHoraDesc(
            UUID idUsuario, EstadoConsulta estado, Pageable pageable);
    Page<Consulta> findAllByOrderByFechaHoraDesc(Pageable pageable);
    Page<Consulta> findByEstadoOrderByFechaHoraDesc(EstadoConsulta estado, Pageable pageable);
}
