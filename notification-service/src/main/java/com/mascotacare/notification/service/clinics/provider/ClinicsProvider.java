package com.mascotacare.notification.service.clinics.provider;

import com.mascotacare.notification.service.clinics.dto.ClinicDto;

import java.util.List;

/** Provider Pattern §C4: estrategia intercambiable Google Maps vs OSM. */
public interface ClinicsProvider {
    String name();
    List<ClinicDto> findNearby(double lat, double lng, int radiusMeters, int limit);
}
