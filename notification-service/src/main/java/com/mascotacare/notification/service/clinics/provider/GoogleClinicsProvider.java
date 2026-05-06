package com.mascotacare.notification.service.clinics.provider;

import com.mascotacare.notification.service.clinics.dto.ClinicDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

/**
 * Implementación con Google Places API (New).
 * Solo se activa si GOOGLE_MAPS_API_KEY está presente. Si no, queda deshabilitado.
 * https://developers.google.com/maps/documentation/places/web-service/nearby-search
 */
@Slf4j
@Component
public class GoogleClinicsProvider implements ClinicsProvider {

    private static final String URL = "https://places.googleapis.com/v1/places:searchNearby";
    private static final String FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location,"
                    + "places.nationalPhoneNumber,places.internationalPhoneNumber";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final String apiKey;

    public GoogleClinicsProvider(@Value("${google.maps.api-key:}") String apiKey,
                                 WebClient.Builder builder) {
        this.apiKey = apiKey;
        this.webClient = builder.baseUrl(URL).build();
    }

    @Override public String name() { return "GOOGLE"; }

    public boolean enabled() { return apiKey != null && !apiKey.isBlank(); }

    @Override
    public List<ClinicDto> findNearby(double lat, double lng, int radius, int limit) {
        if (!enabled()) return List.of();
        try {
            Map<?, ?> resp = webClient.post()
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .bodyValue(buildBody(lat, lng, radius, limit))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();
            return parse(resp, lat, lng);
        } catch (Exception e) {
            log.warn("Google Places falló: {}", e.getMessage());
            return List.of();
        }
    }

    private Map<String, Object> buildBody(double lat, double lng, int radius, int limit) {
        return Map.of(
                "includedTypes", List.of("veterinary_care"),
                "maxResultCount", Math.min(limit, 20),
                "locationRestriction", Map.of("circle", Map.of(
                        "center", Map.of("latitude", lat, "longitude", lng),
                        "radius", (double) radius)));
    }

    @SuppressWarnings("unchecked")
    private List<ClinicDto> parse(Map<?, ?> resp, double srcLat, double srcLng) {
        if (resp == null) return List.of();
        List<Map<String, Object>> places = (List<Map<String, Object>>) resp.get("places");
        if (places == null) return List.of();
        List<ClinicDto> out = new ArrayList<>();
        for (Map<String, Object> p : places) {
            Map<String, Object> loc = (Map<String, Object>) p.get("location");
            Map<String, Object> name = (Map<String, Object>) p.get("displayName");
            if (loc == null) continue;
            double lat = ((Number) loc.get("latitude")).doubleValue();
            double lng = ((Number) loc.get("longitude")).doubleValue();
            String tel = (String) p.getOrDefault("nationalPhoneNumber", p.get("internationalPhoneNumber"));
            out.add(new ClinicDto(
                    (String) p.get("id"),
                    name == null ? "Clínica veterinaria" : (String) name.get("text"),
                    (String) p.getOrDefault("formattedAddress", "Dirección no disponible"),
                    tel, lat, lng, (long) haversine(srcLat, srcLng, lat, lng), name()));
        }
        return out;
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
