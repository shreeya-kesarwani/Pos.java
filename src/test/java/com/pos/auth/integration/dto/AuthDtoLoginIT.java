package com.pos.auth.integration.dto;

import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthDtoLoginIT extends AbstractAuthIntegrationTest {

    @Autowired AuthDto authDto;

    @Test
    void shouldLoginAndSetSecurityContext_happyFlow() throws Exception {
        SignupForm signup = new SignupForm();
        signup.setEmail("  A@B.COM  ");
        signup.setPassword("p");
        authDto.signup(signup);

        LoginForm form = new LoginForm();
        form.setEmail("  A@B.COM  ");
        form.setPassword("p");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);

        AuthData out = authDto.login(form, request);

        assertNotNull(out);
        verify(session).setAttribute(
                eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
                any()
        );
    }

    @Test
    void shouldThrowWhenCredentialsInvalid() {
        LoginForm form = new LoginForm();
        form.setEmail("missing@b.com");
        form.setPassword("wrong");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));

        assertThrows(ApiException.class, () -> authDto.login(form, request));
    }
}