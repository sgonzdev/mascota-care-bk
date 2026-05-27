package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.dto.PageResponse;
import com.mascotacare.symptom.service.dto.SymptomRequest;
import com.mascotacare.symptom.service.dto.SymptomResponse;
import com.mascotacare.symptom.service.dto.TriageFlowRequest;
import com.mascotacare.symptom.service.dto.TriageFlowResponse;
import com.mascotacare.symptom.service.entity.Symptom;
import com.mascotacare.symptom.service.mapper.SymptomMapper;
import com.mascotacare.symptom.service.repository.SymptomRepository;
import com.mascotacare.symptom.service.security.UserContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SymptomService {

    private final SymptomRepository repository;
    private final SymptomNormalizer normalizer;
    private final SymptomMapper mapper;
    private final RulesEngineClient rulesClient;
    private final ConsultaService consultaService;
    private final MetricsClient metrics;

    public SymptomResponse register(SymptomRequest req) {
        List<String> codes = normalizer.normalize(req.descripcionLibre());
        Symptom entity = Symptom.builder()
                .idMascota(req.idMascota())
                .descripcionLibre(req.descripcionLibre())
                .codigosNormalizados(String.join(",", codes))
                .severidadPercibida(req.severidadPercibida())
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    /** Flujo orquestado UC2+UC3: registra el síntoma, dispara triage y persiste Consulta. */
    public TriageFlowResponse registerAndEvaluate(TriageFlowRequest req) {
        List<String> codes = normalizer.normalize(req.descripcionLibre());
        Symptom saved = repository.save(Symptom.builder()
                .idMascota(req.idMascota())
                .descripcionLibre(req.descripcionLibre())
                .codigosNormalizados(String.join(",", codes))
                .severidadPercibida(req.severidadPercibida())
                .build());
        Map<String, Object> triage = rulesClient.evaluate(req.especie(), req.edadMeses(), codes);
        String nivelUrgencia = String.valueOf(triage.get("nivelUrgencia"));
        String accionRecomendada = String.valueOf(triage.get("accionRecomendada"));
        UUID idReglaAplicada = triage.get("idReglaAplicada") == null ? null
                : UUID.fromString(triage.get("idReglaAplicada").toString());
        UUID idUsuario = currentUserUuid();
        if (idUsuario != null) {
            consultaService.persistFromTriage(
                    req.idMascota(), idUsuario, req.descripcionLibre(),
                    nivelUrgencia, accionRecomendada, idReglaAplicada);
        }
        // Métricas (fire-and-forget) — alimentan el dashboard del admin.
        metrics.record("consulta", null, 1);
        metrics.record("urgencia", nivelUrgencia, 1);
        if (idReglaAplicada != null) metrics.record("regla", idReglaAplicada.toString(), 1);

        return new TriageFlowResponse(
                saved.getId(), nivelUrgencia, accionRecomendada, idReglaAplicada,
                mapper.toResponse(saved));
    }

    private UUID currentUserUuid() {
        UserContext.User u = UserContext.get();
        if (u == null || u.id() == null || u.id().isBlank()) return null;
        try { return UUID.fromString(u.id()); } catch (IllegalArgumentException e) { return null; }
    }

    @Transactional(readOnly = true)
    public PageResponse<SymptomResponse> historyOf(UUID idMascota, Pageable pageable) {
        return PageResponse.from(
                repository.findByIdMascotaOrderByFechaReporteDesc(idMascota, pageable)
                        .map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SymptomResponse findById(UUID id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Síntoma " + id + " no encontrado")));
    }
}
