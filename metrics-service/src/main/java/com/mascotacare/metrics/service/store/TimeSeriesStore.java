package com.mascotacare.metrics.service.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Almacén ligero de series temporales sobre Redis (sin RedisTimeSeries).
 * Usa hashes con clave por día: mc:metrics:{evento}:{yyyy-MM-dd} → field:etiqueta value:int.
 */
@Component
@RequiredArgsConstructor
public class TimeSeriesStore {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final StringRedisTemplate redis;

    public void increment(String evento, String etiqueta, long delta) {
        String key = key(evento, LocalDate.now());
        redis.opsForHash().increment(key, etiquetaOrAll(etiqueta), delta);
    }

    public long sumLastDays(String evento, int days) {
        long total = 0;
        for (LocalDate d : lastDays(days)) {
            Map<Object, Object> entries = redis.opsForHash().entries(key(evento, d));
            total += entries.values().stream().mapToLong(v -> Long.parseLong(v.toString())).sum();
        }
        return total;
    }

    public Map<String, Long> sumByLabelLastDays(String evento, int days) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (LocalDate d : lastDays(days)) {
            redis.opsForHash().entries(key(evento, d)).forEach((k, v) ->
                    out.merge(k.toString(), Long.parseLong(v.toString()), Long::sum));
        }
        return out;
    }

    public List<long[]> dailyTotals(String evento, int days) {
        List<long[]> series = new ArrayList<>();
        List<LocalDate> dates = lastDays(days);
        for (LocalDate d : dates) {
            long total = redis.opsForHash().entries(key(evento, d)).values()
                    .stream().mapToLong(v -> Long.parseLong(v.toString())).sum();
            series.add(new long[]{d.toEpochDay(), total});
        }
        return series;
    }

    public List<LocalDate> lastDays(int n) {
        List<LocalDate> result = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) result.add(LocalDate.now().minusDays(i));
        return result;
    }

    private String key(String evento, LocalDate date) {
        return "mc:metrics:" + evento + ":" + FMT.format(date);
    }

    private String etiquetaOrAll(String etiqueta) {
        return (etiqueta == null || etiqueta.isBlank()) ? "_all" : etiqueta;
    }
}
