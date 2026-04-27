package com.mascotacare.metrics.service.service;

import com.mascotacare.metrics.service.dto.DashboardResponse;
import com.mascotacare.metrics.service.dto.EventRequest;
import com.mascotacare.metrics.service.store.TimeSeriesStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private static final String EV_CONSULTA = "consulta";
    private static final String EV_URGENCIA = "urgencia";
    private static final String EV_MEJORA = "mejora";
    private static final String EV_REGLA = "regla";

    private final TimeSeriesStore store;

    public void recordEvent(EventRequest req) {
        store.increment(req.evento(), req.etiqueta(), req.valor());
    }

    public DashboardResponse buildDashboard(int days) {
        long total = store.sumLastDays(EV_CONSULTA, days);
        Map<String, Long> dist = store.sumByLabelLastDays(EV_URGENCIA, days);
        long mejoras = dist.getOrDefault("MEJORO", store.sumLastDays(EV_MEJORA, days));
        double tasa = total == 0 ? 0 : (mejoras * 100.0) / total;

        List<DashboardResponse.DailyPoint> daily = new ArrayList<>();
        var totals = store.dailyTotals(EV_CONSULTA, days);
        for (long[] p : totals) {
            daily.add(new DashboardResponse.DailyPoint(LocalDate.ofEpochDay(p[0]).toString(), p[1]));
        }

        Map<String, Long> reglas = store.sumByLabelLastDays(EV_REGLA, days);
        List<DashboardResponse.TopRule> top = reglas.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new DashboardResponse.TopRule(e.getKey(), e.getValue()))
                .toList();

        return new DashboardResponse(total, dist, Math.round(tasa * 10) / 10.0, daily, top);
    }
}
