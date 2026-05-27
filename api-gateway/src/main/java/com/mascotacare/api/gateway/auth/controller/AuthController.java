package com.mascotacare.api.gateway.auth.controller;

import com.mascotacare.api.gateway.auth.dto.AuthDtos.*;
import com.mascotacare.api.gateway.auth.entity.User;
import com.mascotacare.api.gateway.auth.security.JwtService;
import com.mascotacare.api.gateway.auth.service.AuthService;
import com.mascotacare.api.gateway.auth.service.AuthService.IssuedTokens;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticación", description = "Login, registro, refresh con rotación de tokens y logout. "
        + "Access token JWT HS256 (15 min) + refresh token rotativo en cookie HttpOnly (7 días).")
public class AuthController {

    private static final String COOKIE_NAME = "mc_refresh";
    private final AuthService auth;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario (rol DUEÑO por defecto)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado, devuelve access + cookie refresh"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (email/password/nombre)")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return tokenResponse(auth.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con email y password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login OK, devuelve access + cookie refresh"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "400", description = "Email mal formado o body vacío")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return tokenResponse(auth.login(req), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token (cookie)",
            description = "Lee la cookie HttpOnly mc_refresh, la invalida (rotación) y emite un par nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Cookie ausente, expirada o ya rotada")
    })
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(value = COOKIE_NAME, required = false) String refresh) {
        return tokenResponse(auth.refresh(refresh), HttpStatus.OK);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión: revoca el refresh token y borra la cookie")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    public ResponseEntity<Void> logout(
            @CookieValue(value = COOKIE_NAME, required = false) String refresh) {
        auth.logout(refresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expireCookie().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado actual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido")
    })
    public UserResponse me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return UserResponse.from(requireAuthenticated(authHeader));
    }

    @GetMapping("/users/{id}/contact")
    @Operation(summary = "Datos de contacto públicos de un usuario",
            description = "Solo devuelve nombre, email y teléfono. Pensado para que el "
                    + "dueño pueda contactar al veterinario asignado (RF22).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacto del usuario"),
            @ApiResponse(responseCode = "404", description = "Usuario no existe"),
            @ApiResponse(responseCode = "401", description = "JWT inválido")
    })
    public java.util.Map<String, String> userContact(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @org.springframework.web.bind.annotation.PathVariable UUID id) {
        requireAuthenticated(authHeader);  // cualquier autenticado
        User u = auth.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));
        return java.util.Map.of(
                "id", u.getId().toString(),
                "nombre", u.getNombre() == null ? "" : u.getNombre(),
                "email",  u.getEmail()  == null ? "" : u.getEmail(),
                "telefono", u.getTelefono() == null ? "" : u.getTelefono());
    }

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios (solo admin)",
            description = "Filtro opcional por rol (DUENO|ADMIN|VETERINARIO). Sin parámetro: todos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios sin password"),
            @ApiResponse(responseCode = "403", description = "Solo admin"),
            @ApiResponse(responseCode = "401", description = "JWT inválido")
    })
    public java.util.List<UserResponse> listUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) User.Rol rol) {
        User caller = requireAuthenticated(authHeader);
        if (caller.getRol() != User.Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores");
        }
        return auth.listByRol(rol).stream().map(UserResponse::from).toList();
    }

    /** Valida el JWT y devuelve el User correspondiente. */
    private User requireAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        try {
            String userId = jwtService.parse(authHeader.substring(7)).getSubject();
            return auth.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
