package com.mascotacare.followup.service.scheduler;

import com.mascotacare.followup.service.entity.Followup;
import com.mascotacare.followup.service.repository.FollowupRepository;
import com.mascotacare.followup.service.service.AlertTrigger;
import com.mascotacare.followup.service.service.ConsultaClient;
import com.mascotacare.followup.service.service.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowupScheduler {

    private final FollowupRepository repository;
    private final AlertTrigger alertTrigger;
    private final ConsultaClient consultaClient;
    private final NotificationClient notifClient;

    /** Cada 5 min revisa seguimientos sin mejora ≥48h sin alerta enviada. */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT30S")
    @Transactional
    public void checkPending() {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(48);
        List<Followup> pending = repository
                .findByEstadoAndAlertaEnviadaFalseAndFechaSeguimientoBefore(
                        Followup.Estado.NO_MEJORO, threshold);
        if (pending.isEmpty()) {
            log.debug("scheduler: 0 seguimientos pendientes");
            return;
        }
        log.info("scheduler: enviando alertas para {} seguimientos sin mejora", pending.size());
        for (Followup f : pending) {
            if (alertTrigger.trigger(f)) {
                f.setAlertaEnviada(true);
                repository.save(f);
            }
        }
    }

    /**
     * RF27 — Cada hora: busca consultas activas creadas hace 24-48h sin ningún
     * seguimiento registrado y envía un recordatorio al dueño para que reporte.
     */
    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT2M")
    @Transactional
    public void recordatorioSeguimiento() {
        List<Map<String, Object>> consultas = consultaClient.findOldOpenConsultas(24);
        if (consultas.isEmpty()) return;

        OffsetDateTime min = OffsetDateTime.now().minusHours(48);
        OffsetDateTime max = OffsetDateTime.now().minusHours(24);
        int enviados = 0;

        for (Map<String, Object> c : consultas) {
            try {
                UUID idConsulta = UUID.fromString(c.get("id").toString());
                UUID idUsuario = UUID.fromString(c.get("idUsuario").toString());
                OffsetDateTime fechaHora = consultaClient.parseFechaHora(c.get("fechaHora"));

                if (fechaHora.isAfter(max) || fechaHora.isBefore(min)) continue;
                if (repository.existsByIdConsulta(idConsulta)) continue;

                notifClient.sendPersonal(idUsuario,
                        "¿Cómo está tu mascota?",
                        "Han pasado 24h desde tu consulta. Reporta si tu mascota mejoró, "
                                + "no mejoró o aún no hay cambios. Caso ID: " + idConsulta);
                enviados++;
            } catch (Exception e) {
                log.debug("Error procesando consulta para recordatorio: {}", e.getMessage());
            }
        }
        if (enviados > 0) log.info("scheduler: {} recordatorios de seguimiento enviados", enviados);
    }
}
