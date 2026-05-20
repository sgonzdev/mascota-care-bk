package com.mascotacare.api.gateway.auth.filter;

import com.mascotacare.api.gateway.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Valida el JWT en cada request entrante. Si es válido, inyecta los claims
 * como cabeceras X-User-* para que los microservicios downstream las consuman.
 * Rutas públicas no requieren token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    /** Match exacto (path completo) — NO prefix, así /auth/me NO es público. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login", "/auth/register", "/auth/refresh"
    );

    /** Estos sí matchean por prefijo. */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/actuator", "/eureka",
            "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars",
            "/docs"
    );

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        // CORS preflights deben pasar sin token; el handler de CORS añade los headers.
        if (HttpMethod.OPTIONS.equals(req.getMethod())) {
            return chain.filter(stripUserHeaders(exchange));
        }

        if (isPublic(path)) {
            return chain.filter(stripUserHeaders(exchange));
        }

        String token = extractToken(req, path);
        if (token == null) {
            return reject(exchange, "Token ausente");
        }

        try {
            Claims claims = jwtService.parse(token);
            ServerHttpRequest mutated = req.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", String.valueOf(claims.get("email", String.class)))
                    .header("X-User-Role", String.valueOf(claims.get("rol", String.class)))
                    .header("X-User-Name", String.valueOf(claims.get("nombre", String.class)))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException e) {
            log.debug("JWT inválido: {}", e.getMessage());
            return reject(exchange, "Token inválido o expirado");
        }
    }

    /**
     * Para HTTP normal, lee `Authorization: Bearer …`. Para WebSocket handshake
     * (path /ws/**) el navegador no permite headers custom, así que aceptamos
     * el JWT como query param `?token=…`.
     */
    private String extractToken(ServerHttpRequest req, String path) {
        if (path.startsWith("/ws/") || path.equals("/ws")) {
            String q = req.getQueryParams().getFirst("token");
            if (q != null && !q.isBlank()) return q;
        }
        String auth = req.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return null;
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path)
                || PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /** Quita headers internos para que un cliente no pueda spoofearlos. */
    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Email");
                    h.remove("X-User-Role");
                    h.remove("X-User-Name");
                }).build();
        return exchange.mutate().request(mutated).build();
    }

    private Mono<Void> reject(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
        log.debug("401 → {}", reason);
        return exchange.getResponse().setComplete();
    }

    @Override public int getOrder() { return -100; }
}
