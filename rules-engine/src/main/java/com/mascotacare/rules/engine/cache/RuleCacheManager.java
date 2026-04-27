package com.mascotacare.rules.engine.cache;

import com.mascotacare.rules.engine.entity.Rule;
import com.mascotacare.rules.engine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Implementa el patrón Cache-Aside descrito en C4 §Patrones.
 * Lee primero de Redis; si miss, carga desde Postgres y rellena el cache.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleCacheManager {

    private static final String CACHE_KEY = "mascotacare:rules:active";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RuleRepository repository;
    private final RedisTemplate<String, Object> redis;

    @SuppressWarnings("unchecked")
    public List<Rule> getActiveRules() {
        Object cached = redis.opsForValue().get(CACHE_KEY);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            log.debug("cache HIT — {} reglas", list.size());
            return (List<Rule>) list;
        }
        log.debug("cache MISS — cargando desde Postgres");
        List<Rule> fresh = repository.findByActivaTrueOrderByPrioridadAsc();
        redis.opsForValue().set(CACHE_KEY, fresh, TTL);
        return fresh;
    }

    public void invalidate() {
        redis.delete(CACHE_KEY);
        log.info("cache de reglas invalidado");
    }
}
