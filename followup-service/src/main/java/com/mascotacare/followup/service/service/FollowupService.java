package com.mascotacare.followup.service.service;

import com.mascotacare.followup.service.dto.FollowupRequest;
import com.mascotacare.followup.service.dto.FollowupResponse;
import com.mascotacare.followup.service.dto.PageResponse;
import com.mascotacare.followup.service.entity.Followup;
import com.mascotacare.followup.service.repository.FollowupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowupService {

    private final FollowupRepository repository;
    private final AlertTrigger alertTrigger;

    public FollowupResponse register(FollowupRequest req) {
        Followup f = Followup.builder()
                .idConsulta(req.idConsulta())
                .idMascota(req.idMascota())
                .estado(req.estado())
                .observaciones(req.observaciones())
                .alertaEnviada(false)
                .build();
        Followup saved = repository.save(f);
        if (saved.getEstado() == Followup.Estado.NO_MEJORO) {
            boolean ok = alertTrigger.trigger(saved);
            if (ok) {
                saved.setAlertaEnviada(true);
                repository.save(saved);
            }
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowupResponse> historyOf(UUID idMascota, Pageable pageable) {
        return PageResponse.from(
                repository.findByIdMascotaOrderByFechaSeguimientoDesc(idMascota, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public java.util.List<FollowupResponse> byConsulta(UUID idConsulta) {
        return repository.findByIdConsultaOrderByFechaSeguimientoDesc(idConsulta)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FollowupResponse findById(UUID id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Seguimiento " + id + " no encontrado")));
    }

    private FollowupResponse toResponse(Followup f) {
        return new FollowupResponse(
                f.getId(), f.getIdConsulta(), f.getIdMascota(),
                f.getEstado(), f.getObservaciones(), f.getAlertaEnviada(),
                f.getFechaSeguimiento());
    }
}
