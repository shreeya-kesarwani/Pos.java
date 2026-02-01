package com.pos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // IMPORTANT: servletPath excludes context-path (/api)
        // If context-path=/api, servletPath for /api/auth/session is /auth/session
        String path = request.getServletPath();
        String method = request.getMethod();

        // Allow preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // Allow auth endpoints + swagger/docs always
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // If JWT didn’t authenticate, let SecurityConfig handle 401/permitAll
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ DO NOT clear authentication here.
        // Your role/CSV based authorization should only return 403 if not allowed.

        // TODO: Put your existing "CSV permission check" here.
        // Example placeholder: allow everything for now
        boolean allowed = true;

        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Forbidden\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
