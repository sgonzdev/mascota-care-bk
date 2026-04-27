package com.mascotacare.symptom.service.mapper;

import com.mascotacare.symptom.service.dto.SymptomResponse;
import com.mascotacare.symptom.service.entity.Symptom;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SymptomMapper {

    public SymptomResponse toResponse(Symptom s) {
        List<String> codigos = s.getCodigosNormalizados() == null || s.getCodigosNormalizados().isBlank()
                ? List.of()
                : List.of(s.getCodigosNormalizados().split(","));
        return new SymptomResponse(
                s.getId(), s.getIdMascota(), s.getDescripcionLibre(),
                codigos, s.getSeveridadPercibida(), s.getFechaReporte());
    }
}
