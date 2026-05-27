package com.mascotacare.api.gateway.audit.controller;

import com.mascotacare.api.gateway.audit.dto.AuditEntryDto;
import com.mascotacare.api.gateway.audit.service.AuditService;
import com.mascotacare.api.gateway.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints de consulta de auditoría (RF26). Restringido a ADMIN.
 *
 * Al ser un controlador local del gateway (no una ruta proxyada), los GlobalFilter
 * de Spring Cloud Gateway no se ejecutan antes y por tanto no llegan headers
 * X-User-*. Parseamos el JWT directamente como hace AuthController.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;
    private final JwtService jwtService;

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) UUID idUsuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) OffsetDateTime desde,
            @RequestParam(required = false) OffsetDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        requireAdmin(authHeader);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<AuditEntryDto> result = service.search(idUsuario, accion, desde, hasta, pageable)
                .map(AuditEntryDto::of);

        return Map.of(
                "content", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "hasMore", !result.isLast());
    }

    private void requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        try {
            Claims claims = jwtService.parse(authHeader.substring(7));
            String rol = claims.get("rol", String.class);
            if (!"ADMIN".equalsIgnoreCase(rol)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores");
            }
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT inválido o expirado");
        }
    }
}
