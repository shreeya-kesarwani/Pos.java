package com.pos.security;

import com.pos.model.constants.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = JwtUtil.parse(token); // ✅ static

                Integer userId = Integer.valueOf(claims.getSubject());
                String roleStr = (String) claims.get("role");
                UserRole role = UserRole.valueOf(roleStr);

                AuthPrincipal principal = new AuthPrincipal(userId, role);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            } catch (IllegalStateException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
