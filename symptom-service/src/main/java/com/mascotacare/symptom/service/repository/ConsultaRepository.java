package com.mascotacare.symptom.service.repository;

import com.mascotacare.symptom.service.entity.Consulta;
import com.mascotacare.symptom.service.entity.Consulta.EstadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    List<Consulta> findByIdUsuarioOrderByFechaHoraDesc(UUID idUsuario);

    List<Consulta> findByIdUsuarioAndEstadoOrderByFechaHoraDesc(UUID idUsuario, EstadoConsulta estado);

    List<Consulta> findAllByOrderByFechaHoraDesc();

    List<Consulta> findByEstadoOrderByFechaHoraDesc(EstadoConsulta estado);
}
