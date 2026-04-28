package com.mascotacare.notification.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)
public class UserContextFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of("/actuator", "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getRequestURI();
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(req, res);
            return;
        }
        String userId = req.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader("Content-Type", "application/json");
            res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"X-User-Id missing\"}");
            return;
        }
        UserContext.set(new UserContext.User(
                userId,
                req.getHeader("X-User-Email"),
                req.getHeader("X-User-Role"),
                req.getHeader("X-User-Name")));
        try {
            chain.doFilter(req, res);
        } finally {
            UserContext.clear();
        }
    }
}
