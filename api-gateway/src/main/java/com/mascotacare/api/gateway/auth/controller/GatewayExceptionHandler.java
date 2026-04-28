package com.mascotacare.api.gateway.auth.controller;

import com.mascotacare.api.gateway.auth.service.AuthService.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
        HttpStatus status = switch (e.code) {
            case "BAD_CREDENTIALS", "REFRESH_INVALID", "REFRESH_MISSING" -> HttpStatus.UNAUTHORIZED;
            case "EMAIL_TAKEN" -> HttpStatus.CONFLICT;
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return body(status, e.getMessage(), Map.of("code", e.code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream().collect(
                Collectors.toMap(f -> f.getField(),
                        f -> f.getDefaultMessage() == null ? "inválido" : f.getDefaultMessage(),
                        (a, b) -> a));
        return body(HttpStatus.BAD_REQUEST, "Datos inválidos", Map.of("fields", fields));
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus s, String msg, Object details) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", OffsetDateTime.now());
        b.put("status", s.value());
        b.put("error", s.getReasonPhrase());
        b.put("message", msg);
        if (details != null) b.put("details", details);
        return ResponseEntity.status(s).body(b);
    }
}
