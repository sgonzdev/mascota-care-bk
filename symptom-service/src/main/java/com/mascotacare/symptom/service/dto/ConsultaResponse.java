package com.mascotacare.symptom.service.dto;

import com.mascotacare.symptom.service.entity.Consulta;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaResponse(
        UUID id,
        UUID idMascota,
        UUID idUsuario,
        OffsetDateTime fechaHora,
        String descripcionSintomas,
        Consulta.NivelUrgencia nivelUrgencia,
        String respuestaGenerada,
        UUID idReglaAplicada,
        String canal,
        Consulta.EstadoConsulta estado,
        String notasInternas,
        OffsetDateTime actualizadaEn
) {
    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(
                c.getId(), c.getIdMascota(), c.getIdUsuario(), c.getFechaHora(),
                c.getDescripcionSintomas(), c.getNivelUrgencia(), c.getRespuestaGenerada(),
                c.getIdReglaAplicada(), c.getCanal(), c.getEstado(), c.getNotasInternas(),
                c.getActualizadaEn());
    }
}
