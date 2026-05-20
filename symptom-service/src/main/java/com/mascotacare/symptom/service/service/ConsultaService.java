package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.dto.ConsultaPatchRequest;
import com.mascotacare.symptom.service.dto.ConsultaResponse;
import com.mascotacare.symptom.service.dto.PageResponse;
import com.mascotacare.symptom.service.entity.Consulta;
import com.mascotacare.symptom.service.entity.Consulta.EstadoConsulta;
import com.mascotacare.symptom.service.entity.Consulta.NivelUrgencia;
import com.mascotacare.symptom.service.repository.ConsultaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultaService {

    private final ConsultaRepository repository;

    /**
     * Persiste una consulta resultante de un triage UC2+UC3.
     * Llamado desde {@link SymptomService#registerAndEvaluate}.
     */
    public Consulta persistFromTriage(
            UUID idMascota,
            UUID idUsuario,
            String descripcionSintomas,
            String nivelUrgenciaStr,
            String respuestaGenerada,
            UUID idReglaAplicada) {
        NivelUrgencia nivel = parseNivel(nivelUrgenciaStr);
        EstadoConsulta estado = nivel == NivelUrgencia.ALTA ? EstadoConsulta.pendiente : EstadoConsulta.activa;
        return repository.save(Consulta.builder()
                .idMascota(idMascota)
                .idUsuario(idUsuario)
                .descripcionSintomas(descripcionSintomas)
                .nivelUrgencia(nivel)
                .respuestaGenerada(respuestaGenerada)
                .idReglaAplicada(idReglaAplicada)
                .canal("web")
                .estado(estado)
                .notasInternas("")
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<ConsultaResponse> list(UUID idUsuario, String estadoFilter, Pageable pageable) {
        EstadoConsulta estado = parseEstado(estadoFilter);
        Page<Consulta> rows;
        if (idUsuario != null && estado != null) {
            rows = repository.findByIdUsuarioAndEstadoOrderByFechaHoraDesc(idUsuario, estado, pageable);
        } else if (idUsuario != null) {
            rows = repository.findByIdUsuarioOrderByFechaHoraDesc(idUsuario, pageable);
        } else if (estado != null) {
            rows = repository.findByEstadoOrderByFechaHoraDesc(estado, pageable);
        } else {
            rows = repository.findAllByOrderByFechaHoraDesc(pageable);
        }
        return PageResponse.from(rows.map(ConsultaResponse::from));
    }

    @Transactional(readOnly = true)
    public ConsultaResponse get(UUID id) {
        return ConsultaResponse.from(find(id));
    }

    public ConsultaResponse patch(UUID id, ConsultaPatchRequest req) {
        Consulta c = find(id);
        if (req.estado() != null) c.setEstado(req.estado());
        if (req.notasInternas() != null) c.setNotasInternas(req.notasInternas());
        return ConsultaResponse.from(repository.save(c));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) throw notFound(id);
        repository.deleteById(id);
    }

    private Consulta find(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    private EntityNotFoundException notFound(UUID id) {
        return new EntityNotFoundException("Consulta " + id + " no encontrada");
    }

    private NivelUrgencia parseNivel(String s) {
        try { return NivelUrgencia.valueOf(s); } catch (Exception e) { return NivelUrgencia.MEDIA; }
    }

    private EstadoConsulta parseEstado(String s) {
        if (s == null || s.isBlank() || "all".equalsIgnoreCase(s)) return null;
        try { return EstadoConsulta.valueOf(s); } catch (Exception e) { return null; }
    }
}
