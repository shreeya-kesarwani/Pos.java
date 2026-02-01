package com.pos.config;

import com.pos.security.JwtAuthFilter;
import com.pos.security.JwtUtil;
import com.pos.security.RoleAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiChain(
            HttpSecurity http,
            JwtUtil jwtUtil,
            RoleAuthorizationFilter roleAuthorizationFilter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ✅ Enable CORS (uses CorsConfig bean)
                .cors(Customizer.withDefaults())

                // ✅ JWT stateless API
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ Proper JSON 401 / 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"You are not logged in\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"message\":\"Only supervisors can upload or edit master data.\"}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/auth/session").authenticated()
                        .anyRequest().authenticated()
                )

                // 1️⃣ JWT authentication
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 2️⃣ Role authorization
                .addFilterAfter(
                        roleAuthorizationFilter,
                        JwtAuthFilter.class
                );

        return http.build();
    }
}
