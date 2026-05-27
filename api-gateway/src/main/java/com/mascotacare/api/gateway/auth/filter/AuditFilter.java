package com.mascotacare.api.gateway.auth.filter;

import com.mascotacare.api.gateway.audit.entity.AuditEntry;
import com.mascotacare.api.gateway.audit.service.AuditService;
import com.mascotacare.api.gateway.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

/**
 * Logger / Auditoría (componente §C4 Capa 2, RF26): registra cada request
 * que pasa por el gateway con método, path, status, latencia y user, y
 * persiste las acciones críticas en audit_log.
 *
 * Implementado como WebFilter (no GlobalFilter) para capturar TAMBIÉN las
 * llamadas a los controladores locales (/auth/login, /auth/me, etc.) —
 * los GlobalFilter de Spring Cloud Gateway solo aplican a rutas routeadas.
 *
 * Solo se persisten métodos mutadores (POST/PUT/PATCH/DELETE) o lecturas
 * explícitamente sensibles — los GET de listas se mantienen solo a stdout
 * para no inundar la tabla.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditFilter implements WebFilter, Ordered {

    private final AuditService auditService;
    private final JwtService jwtService;

    /** Paths que, aunque sean GET, queremos auditar (acceso a info sensible). */
    private static final Set<String> SENSITIVE_GET_PREFIXES = Set.of(
            "/api/audit",
            "/auth/users",
            "/auth/me"
    );

    /** Paths totalmente ignorados (health/static/ws). */
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator",
            "/ws/",
            "/eureka/",
            "/v3/api-docs",
            "/swagger-ui",
            "/docs/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        return chain.filter(exchange).doFinally(sig -> {
            long ms = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() == null ? 0
                    : exchange.getResponse().getStatusCode().value();
            // Las cabeceras X-User-* las inyecta JwtGatewayFilter solo para rutas
            // proxyadas. Para los controladores locales (/auth/**, /api/audit)
            // parseamos el JWT directamente como fallback.
            String userId = req.getHeaders().getFirst("X-User-Id");
            String email  = req.getHeaders().getFirst("X-User-Email");
            String role   = req.getHeaders().getFirst("X-User-Role");
            if (userId == null) {
                Claims c = tryParseJwt(req);
                if (c != null) {
                    userId = c.getSubject();
                    email  = c.get("email", String.class);
                    role   = c.get("rol", String.class);
                }
            }

            log.info("AUDIT method={} path={} status={} latencyMs={} userId={}",
                    req.getMethod(), path, status, ms,
                    userId == null ? "anonymous" : userId);

            if (shouldPersist(req.getMethod(), path)) {
                auditService.record(AuditEntry.builder()
                        .idUsuario(parseUuid(userId))
                        .email(email)
                        .rol(role)
                        .accion(deriveAction(req.getMethod(), path))
                        .metodo(req.getMethod() == null ? null : req.getMethod().name())
                        .path(truncate(path, 500))
                        .statusCode(status == 0 ? null : status)
                        .ip(clientIp(req))
                        .userAgent(truncate(req.getHeaders().getFirst("User-Agent"), 300))
                        .detalle(null)
                        .build());
            }
        });
    }

    private boolean shouldPersist(HttpMethod method, String path) {
        if (path == null || method == null) return false;
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) return false;
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT
                || method == HttpMethod.PATCH || method == HttpMethod.DELETE) {
            return true;
        }
        if (method == HttpMethod.GET) {
            for (String p : SENSITIVE_GET_PREFIXES) {
                if (path.startsWith(p)) return true;
            }
        }
        return false;
    }

    /**
     * Mapea path + método a un código de acción legible. Casos especiales
     * priman; el resto cae a un default `<segmento>_<verbo>`.
     */
    private String deriveAction(HttpMethod method, String path) {
        String verb = method == null ? "UNKNOWN" : method.name();
        if (path == null || path.isBlank()) return verb;

        if (path.startsWith("/auth/login"))    return "AUTH_LOGIN";
        if (path.startsWith("/auth/register")) return "AUTH_REGISTER";
        if (path.startsWith("/auth/refresh"))  return "AUTH_REFRESH";
        if (path.startsWith("/auth/logout"))   return "AUTH_LOGOUT";
        if (path.contains("/claim"))           return "CONSULTA_CLAIM";
        if (path.contains("/release"))         return "CONSULTA_RELEASE";
        if (path.contains("/archive"))         return "CONSULTA_ARCHIVE";
        if (path.contains("/followups"))       return method == HttpMethod.POST ? "FOLLOWUP_CREATE" : "FOLLOWUP_" + verb;
        if (path.startsWith("/api/rules"))     return "RULE_" + verb;
        if (path.startsWith("/api/guides"))    return "GUIDE_" + verb;
        if (path.startsWith("/api/attachments")) return "ATTACHMENT_" + verb;
        if (path.startsWith("/api/notifications/broadcast")) return "NOTIFICATION_BROADCAST";
        if (path.startsWith("/api/audit"))     return "AUDIT_READ";

        String[] parts = path.split("/");
        for (String p : parts) {
            if (!p.isBlank()) return p.toUpperCase() + "_" + verb;
        }
        return verb;
    }

    private Claims tryParseJwt(ServerHttpRequest req) {
        String auth = req.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try { return jwtService.parse(auth.substring(7)); }
            catch (JwtException ignored) {}
        }
        return null;
    }

    private static UUID parseUuid(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException ex) { return null; }
    }

    private static String clientIp(ServerHttpRequest req) {
        String xff = req.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddress() == null ? null
                : req.getRemoteAddress().getAddress().getHostAddress();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /** Después del JwtFilter (-100), de Spring Security y de CORS. */
    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
