package com.mascotacare.api.gateway.auth.dto;

import com.mascotacare.api.gateway.auth.entity.User;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 80) String nombre,
            @Email @NotBlank @Size(max = 200) String email,
            @Size(max = 20) String telefono,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            long expiresInSeconds,
            UserResponse user
    ) {}

    public record UserResponse(
            UUID id,
            String nombre,
            String email,
            String telefono,
            User.Rol rol,
            OffsetDateTime fechaRegistro
    ) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getNombre(), u.getEmail(),
                    u.getTelefono(), u.getRol(), u.getFechaRegistro());
        }
    }
}
