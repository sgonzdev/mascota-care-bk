package com.mascotacare.api.gateway.auth.controller;

import com.mascotacare.api.gateway.auth.dto.AuthDtos.*;
import com.mascotacare.api.gateway.auth.entity.User;
import com.mascotacare.api.gateway.auth.security.JwtService;
import com.mascotacare.api.gateway.auth.service.AuthService;
import com.mascotacare.api.gateway.auth.service.AuthService.IssuedTokens;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "mc_refresh";
    private final AuthService auth;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return tokenResponse(auth.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return tokenResponse(auth.login(req), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(value = COOKIE_NAME, required = false) String refresh) {
        return tokenResponse(auth.refresh(refresh), HttpStatus.OK);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logout(
            @CookieValue(value = COOKIE_NAME, required = false) String refresh) {
        auth.logout(refresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expireCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        try {
            String userId = jwtService.parse(authHeader.substring(7)).getSubject();
            User user = auth.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            return UserResponse.from(user);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }

    private ResponseEntity<AuthResponse> tokenResponse(IssuedTokens t, HttpStatus status) {
        AuthResponse body = new AuthResponse(
                t.accessToken(), t.accessTtlSeconds(), UserResponse.from(t.user()));
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshCookie(t.refreshToken(), t.refreshTtlDays()).toString())
                .body(body);
    }

    private ResponseCookie refreshCookie(String value, long days) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(false)               // true en producción HTTPS
                .sameSite("Lax")
                .path("/auth")
                .maxAge(Duration.ofDays(days))
                .build();
    }

    private ResponseCookie expireCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true).path("/auth").maxAge(Duration.ZERO).build();
    }
}
