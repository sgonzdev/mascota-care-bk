package com.mascotacare.notification.service.clinics.service;

import com.mascotacare.notification.service.clinics.dto.ClinicDto;
import com.mascotacare.notification.service.clinics.provider.GoogleClinicsProvider;
import com.mascotacare.notification.service.clinics.provider.OsmClinicsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Estrategia: si Google está habilitado lo prefiere; si no o falla, fallback a OSM.
 * §C4 Provider Pattern aplicado a búsqueda geoespacial.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicsService {

    private final GoogleClinicsProvider google;
    private final OsmClinicsProvider osm;

    public List<ClinicDto> findNearby(double lat, double lng, int radius, int limit) {
        if (google.enabled()) {
            List<ClinicDto> out = google.findNearby(lat, lng, radius, limit);
            if (!out.isEmpty()) return out;
            log.info("Google no devolvió resultados, probando OSM");
        }
        return osm.findNearby(lat, lng, radius, limit);
    }
}
