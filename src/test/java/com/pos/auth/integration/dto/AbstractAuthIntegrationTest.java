package com.pos.auth.integration.dto;

import com.pos.setup.AbstractIntegrationTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractAuthIntegrationTest extends AbstractIntegrationTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected void authenticate(Integer userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }

    protected void authenticatePrincipal(Object principal, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }

    protected HttpServletRequest mockRequestWithSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);
        return request;
    }
}