package com.pos.security;

import com.pos.utils.CsvRoleAccessService;
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

    private final CsvRoleAccessService csvService;

    public RoleAuthorizationFilter(CsvRoleAccessService csvService) {
        this.csvService = csvService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath(); // excludes context-path (/api)
        String method = request.getMethod();

        // Allow preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // Allow auth endpoints + swagger/docs always
        // Also allow Spring's error endpoint so you don't block error rendering
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If JWT didn’t authenticate, let SecurityConfig handle 401/permitAll
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof AuthPrincipal)) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod().toUpperCase();
        String path = request.getServletPath(); // already without /api if context-path=/api

        // getRole() is likely an enum (UserRole) -> convert to String
        String role = ((AuthPrincipal) auth.getPrincipal()).getRole().toString();

        boolean allowed = csvService.isAllowed(method, path, role);

        if (!allowed) {
            // Return 403 JSON (frontend expects JSON message)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Forbidden\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
