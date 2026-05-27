package com.mascotacare.symptom.service.repository;

import com.mascotacare.symptom.service.entity.Consulta;
import com.mascotacare.symptom.service.entity.Consulta.EstadoConsulta;
import com.mascotacare.symptom.service.entity.Consulta.NivelUrgencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    Page<Consulta> findByIdUsuarioOrderByFechaHoraDesc(UUID idUsuario, Pageable pageable);
    Page<Consulta> findByIdUsuarioAndEstadoOrderByFechaHoraDesc(
            UUID idUsuario, EstadoConsulta estado, Pageable pageable);
    Page<Consulta> findAllByOrderByFechaHoraDesc(Pageable pageable);
    Page<Consulta> findByEstadoOrderByFechaHoraDesc(EstadoConsulta estado, Pageable pageable);
    Page<Consulta> findByNivelUrgenciaOrderByFechaHoraDesc(NivelUrgencia nivel, Pageable pageable);
    Page<Consulta> findByNivelUrgenciaAndEstadoOrderByFechaHoraDesc(
            NivelUrgencia nivel, EstadoConsulta estado, Pageable pageable);

    /**
     * VETERINARIO: casos sin asignar o asignados a este vet, ordenados por fecha.
     * Sólo de la urgencia indicada.
     */
    @Query("select c from Consulta c where c.nivelUrgencia = :nivel "
            + "and (c.idVetAsignado is null or c.idVetAsignado = :idVet) "
            + "order by c.fechaHora desc")
    Page<Consulta> findByNivelUrgenciaAndVetAvailable(
            @Param("nivel") NivelUrgencia nivel,
            @Param("idVet") UUID idVet,
            Pageable pageable);

    /**
     * VETERINARIO: TODOS los casos disponibles para él (libres o suyos),
     * sin filtro de urgencia. Para que el vet pueda tomar casos de cualquier prioridad.
     */
    @Query("select c from Consulta c "
            + "where (c.idVetAsignado is null or c.idVetAsignado = :idVet) "
            + "order by c.fechaHora desc")
    Page<Consulta> findVetAvailable(
            @Param("idVet") UUID idVet,
            Pageable pageable);

    /** VETERINARIO + estado: disponibles para él en un estado concreto. */
    @Query("select c from Consulta c "
            + "where c.estado = :estado "
            + "and (c.idVetAsignado is null or c.idVetAsignado = :idVet) "
            + "order by c.fechaHora desc")
    Page<Consulta> findByEstadoAndVetAvailable(
            @Param("estado") EstadoConsulta estado,
            @Param("idVet") UUID idVet,
            Pageable pageable);
}
