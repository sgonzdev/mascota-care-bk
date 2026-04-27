package com.mascotacare.followup.service.scheduler;

import com.mascotacare.followup.service.entity.Followup;
import com.mascotacare.followup.service.repository.FollowupRepository;
import com.mascotacare.followup.service.service.AlertTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowupScheduler {

    private final FollowupRepository repository;
    private final AlertTrigger alertTrigger;

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
}
