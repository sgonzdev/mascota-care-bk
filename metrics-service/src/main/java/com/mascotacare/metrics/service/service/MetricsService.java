package com.mascotacare.metrics.service.service;

import com.mascotacare.metrics.service.dto.DashboardResponse;
import com.mascotacare.metrics.service.dto.EventRequest;
import com.mascotacare.metrics.service.store.TimeSeriesStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final TimeSeriesStore store;
    private final KPICalculator kpi;

    public void recordEvent(EventRequest req) {
        store.increment(req.evento(), req.etiqueta(), req.valor());
    }

    public DashboardResponse buildDashboard(int days) {
        return new DashboardResponse(
                kpi.totalConsultas(days),
                kpi.distribucionUrgencia(days),
                kpi.tasaMejora(days),
                kpi.consultasPorDia(days),
                kpi.topReglas(days));
    }
}
