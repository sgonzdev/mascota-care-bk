package com.mascotacare.api.gateway.auth.service;

import com.mascotacare.api.gateway.auth.dto.AuthDtos.*;
import com.mascotacare.api.gateway.auth.entity.RefreshToken;
import com.mascotacare.api.gateway.auth.entity.User;
import com.mascotacare.api.gateway.auth.repository.RefreshTokenRepository;
import com.mascotacare.api.gateway.auth.repository.UserRepository;
import com.mascotacare.api.gateway.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    @Value("${jwt.refresh-ttl-days:7}")
    private long refreshTtlDays;

    public IssuedTokens register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new BusinessException("EMAIL_TAKEN", "Email ya registrado");
        }
        User user = users.save(User.builder()
                .nombre(req.nombre())
                .email(req.email())
                .telefono(req.telefono())
                .passwordHash(encoder.encode(req.password()))
                .rol(User.Rol.DUENO)
                .build());
        return issueTokens(user);
    }

    public IssuedTokens login(LoginRequest req) {
        User user = users.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("BAD_CREDENTIALS", "Credenciales inválidas"));
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Credenciales inválidas");
        }
        return issueTokens(user);
    }

    public IssuedTokens refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException("REFRESH_MISSING", "Refresh token ausente");
        }
        String hash = sha256(rawToken);
        RefreshToken stored = refreshTokens.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("REFRESH_INVALID", "Refresh inválido"));
        if (stored.getRevocado() || stored.getExpiraEn().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("REFRESH_INVALID", "Refresh expirado o revocado");
        }
        // Rotación: revoca el actual y emite uno nuevo
        stored.setRevocado(true);
        User user = users.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario no existe"));
        return issueTokens(user);
    }

    public void logout(String rawToken) {
        if (rawToken == null) return;
        refreshTokens.findByTokenHash(sha256(rawToken)).ifPresent(t -> t.setRevocado(true));
    }

    public Optional<User> findById(UUID id) {
        return users.findById(id);
    }

    private IssuedTokens issueTokens(User user) {
        String access = jwtService.issueAccess(user);
        String refreshRaw = randomToken();
        refreshTokens.save(RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(sha256(refreshRaw))
                .expiraEn(OffsetDateTime.now().plus(Duration.ofDays(refreshTtlDays)))
                .revocado(false)
                .build());
        return new IssuedTokens(user, access, refreshRaw, jwtService.accessTtl(), refreshTtlDays);
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public record IssuedTokens(User user, String accessToken, String refreshToken,
                                long accessTtlSeconds, long refreshTtlDays) {}

    public static class BusinessException extends RuntimeException {
        public final String code;
        public BusinessException(String code, String msg) { super(msg); this.code = code; }
    }
}
