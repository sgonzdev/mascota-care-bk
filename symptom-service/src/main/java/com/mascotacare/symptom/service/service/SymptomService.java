package com.mascotacare.symptom.service.service;

import com.mascotacare.symptom.service.dto.SymptomRequest;
import com.mascotacare.symptom.service.dto.SymptomResponse;
import com.mascotacare.symptom.service.dto.TriageFlowRequest;
import com.mascotacare.symptom.service.dto.TriageFlowResponse;
import com.mascotacare.symptom.service.entity.Symptom;
import com.mascotacare.symptom.service.mapper.SymptomMapper;
import com.mascotacare.symptom.service.repository.SymptomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    /** Flujo orquestado UC2+UC3: registra el síntoma y dispara triage automáticamente. */
    public TriageFlowResponse registerAndEvaluate(TriageFlowRequest req) {
        List<String> codes = normalizer.normalize(req.descripcionLibre());
        Symptom saved = repository.save(Symptom.builder()
                .idMascota(req.idMascota())
                .descripcionLibre(req.descripcionLibre())
                .codigosNormalizados(String.join(",", codes))
                .severidadPercibida(req.severidadPercibida())
                .build());
        Map<String, Object> triage = rulesClient.evaluate(req.especie(), req.edadMeses(), codes);
        return new TriageFlowResponse(
                saved.getId(),
                String.valueOf(triage.get("nivelUrgencia")),
                String.valueOf(triage.get("accionRecomendada")),
                triage.get("idReglaAplicada") == null ? null
                        : UUID.fromString(triage.get("idReglaAplicada").toString()),
                mapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public List<SymptomResponse> historyOf(UUID idMascota) {
        return repository.findByIdMascotaOrderByFechaReporteDesc(idMascota).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SymptomResponse findById(UUID id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Síntoma " + id + " no encontrado")));
    }
}
