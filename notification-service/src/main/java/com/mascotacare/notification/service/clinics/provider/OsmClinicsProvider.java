package com.mascotacare.notification.service.clinics.provider;

import com.mascotacare.notification.service.clinics.dto.ClinicDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

/**
 * Implementación con OpenStreetMap Overpass API.
 * Sin API key, sin tarjeta de crédito. Datos comunitarios — cobertura buena en España.
 * Endpoint: https://overpass-api.de/api/interpreter
 */
@Slf4j
@Component
public class OsmClinicsProvider implements ClinicsProvider {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private final WebClient webClient;

    public OsmClinicsProvider(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(OVERPASS_URL).build();
    }

    @Override public String name() { return "OSM"; }

    @Override
    public List<ClinicDto> findNearby(double lat, double lng, int radius, int limit) {
        String query = """
                [out:json][timeout:10];
                (
                  node["amenity"="veterinary"](around:%d,%f,%f);
                  way["amenity"="veterinary"](around:%d,%f,%f);
                );
                out center %d;
                """.formatted(radius, lat, lng, radius, lat, lng, limit);
        try {
            Map<?, ?> resp = webClient.post()
                    .header("Content-Type", "text/plain")
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();
            return parse(resp, lat, lng);
        } catch (Exception e) {
            log.warn("OSM Overpass falló: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ClinicDto> parse(Map<?, ?> resp, double srcLat, double srcLng) {
        if (resp == null) return List.of();
        List<Map<String, Object>> elements = (List<Map<String, Object>>) resp.get("elements");
        if (elements == null) return List.of();
        List<ClinicDto> out = new ArrayList<>();
        for (Map<String, Object> el : elements) {
            double[] coords = extractCoords(el);
            if (coords == null) continue;
            Map<String, Object> tags = (Map<String, Object>) el.getOrDefault("tags", Map.of());
            String id = el.get("type") + "/" + el.get("id");
            String nombre = (String) tags.getOrDefault("name", "Clínica veterinaria");
            String direccion = buildAddress(tags);
            String tel = (String) tags.getOrDefault("phone", tags.get("contact:phone"));
            long dist = (long) haversine(srcLat, srcLng, coords[0], coords[1]);
            out.add(new ClinicDto(id, nombre, direccion, tel, coords[0], coords[1], dist, name()));
        }
        out.sort(Comparator.comparingLong(ClinicDto::distanciaMetros));
        return out;
    }

    private double[] extractCoords(Map<String, Object> el) {
        if (el.get("lat") != null && el.get("lon") != null) {
            return new double[]{toDouble(el.get("lat")), toDouble(el.get("lon"))};
        }
        Map<?, ?> center = (Map<?, ?>) el.get("center");
        if (center != null) return new double[]{toDouble(center.get("lat")), toDouble(center.get("lon"))};
        return null;
    }

    private String buildAddress(Map<String, Object> tags) {
        String street = (String) tags.get("addr:street");
        String num = (String) tags.get("addr:housenumber");
        String city = (String) tags.get("addr:city");
        if (street == null && city == null) return "Dirección no disponible";
        return Stream.of(street, num, city).filter(Objects::nonNull).reduce((a, b) -> a + " " + b).orElse("");
    }

    private double toDouble(Object o) { return o instanceof Number n ? n.doubleValue() : Double.parseDouble(o.toString()); }

    /** Fórmula Haversine para distancia entre 2 puntos GPS, devuelve metros. */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static class Stream {
        static <T> java.util.stream.Stream<T> of(T... values) { return java.util.stream.Stream.of(values); }
    }
}
