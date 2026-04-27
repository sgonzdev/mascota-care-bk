package com.mascotacare.metrics.service.controller;

import com.mascotacare.metrics.service.dto.DashboardResponse;
import com.mascotacare.metrics.service.dto.EventRequest;
import com.mascotacare.metrics.service.service.MetricsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService service;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordEvent(@Valid @RequestBody EventRequest req) {
        service.recordEvent(req);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(defaultValue = "7") int days) {
        return service.buildDashboard(days);
    }
}
