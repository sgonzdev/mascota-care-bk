package com.mascotacare.metrics.service.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalConsultas,
        Map<String, Long> distribucionUrgencia,
        double tasaMejora,
        List<DailyPoint> consultasPorDia,
        List<TopRule> topReglas
) {
    public record DailyPoint(String fecha, long total) {}
    public record TopRule(String reglaId, long activaciones) {}
}
