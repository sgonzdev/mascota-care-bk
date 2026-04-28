package com.mascotacare.metrics.service.service;

import com.mascotacare.metrics.service.dto.DashboardResponse;
import com.mascotacare.metrics.service.store.TimeSeriesStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * KPICalculator (componente §C4 Metrics): calcula los indicadores
 * derivados a partir de los almacenes de eventos en Redis.
 */
@Component
@RequiredArgsConstructor
public class KPICalculator {

    private static final String EV_CONSULTA = "consulta";
    private static final String EV_URGENCIA = "urgencia";
    private static final String EV_MEJORA = "mejora";
    private static final String EV_REGLA = "regla";
    private static final int TOP_N = 5;

    private final TimeSeriesStore store;

    public long totalConsultas(int days) { return store.sumLastDays(EV_CONSULTA, days); }

    public Map<String, Long> distribucionUrgencia(int days) {
        return store.sumByLabelLastDays(EV_URGENCIA, days);
    }

    public double tasaMejora(int days) {
        long total = totalConsultas(days);
        if (total == 0) return 0;
        long mejoras = store.sumByLabelLastDays(EV_URGENCIA, days)
                .getOrDefault("MEJORO", store.sumLastDays(EV_MEJORA, days));
        double tasa = (mejoras * 100.0) / total;
        return Math.round(tasa * 10) / 10.0;
    }

    public List<DashboardResponse.DailyPoint> consultasPorDia(int days) {
        List<DashboardResponse.DailyPoint> daily = new ArrayList<>();
        for (long[] p : store.dailyTotals(EV_CONSULTA, days)) {
            daily.add(new DashboardResponse.DailyPoint(LocalDate.ofEpochDay(p[0]).toString(), p[1]));
        }
        return daily;
    }

    public List<DashboardResponse.TopRule> topReglas(int days) {
        return store.sumByLabelLastDays(EV_REGLA, days).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> new DashboardResponse.TopRule(e.getKey(), e.getValue()))
                .toList();
    }
}
