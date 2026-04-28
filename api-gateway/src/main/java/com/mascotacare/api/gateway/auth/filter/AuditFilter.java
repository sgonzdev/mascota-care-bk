package com.mascotacare.api.gateway.auth.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logger / Auditoría (componente §C4 Capa 2): registra cada request
 * que pasa por el Gateway con método, path, status, latencia y user.
 * Corre DESPUÉS del JwtFilter para capturar X-User-Id si existe.
 */
@Slf4j
@Component
public class AuditFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest req = exchange.getRequest();
        return chain.filter(exchange).doFinally(sig -> {
            long ms = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() == null ? 0
                    : exchange.getResponse().getStatusCode().value();
            String userId = req.getHeaders().getFirst("X-User-Id");
            log.info("AUDIT method={} path={} status={} latencyMs={} userId={}",
                    req.getMethod(), req.getURI().getPath(), status, ms,
                    userId == null ? "anonymous" : userId);
        });
    }

    /** Después del JwtFilter (-100). */
    @Override public int getOrder() { return -50; }
}
