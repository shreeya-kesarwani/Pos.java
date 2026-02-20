package com.pos.integration.dto;

import com.pos.api.AuthApi;
import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.ErrorMessages;
import com.pos.model.constants.UserRole;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthDtoTest {

    @Mock private AuthApi authApi;
    @Mock private Validator validator;

    @InjectMocks private AuthDto authDto;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signupValidForm() throws Exception {
        SignupForm form = new SignupForm();
        form.setEmail("  TEST@EXAMPLE.COM  ");
        form.setPassword("pass");

        when(validator.validate(any(SignupForm.class))).thenReturn(Set.of());

        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setRole(UserRole.SUPERVISOR);

        when(authApi.signup("test@example.com", "pass")).thenReturn(user);

        AuthData out = authDto.signup(form);

        assertNotNull(out);
        verify(authApi).signup("test@example.com", "pass");
    }

    @Test
    void signupValidationFails() {
        SignupForm form = new SignupForm();

        @SuppressWarnings("unchecked")
        ConstraintViolation<SignupForm> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("invalid");
        when(validator.validate(any(SignupForm.class))).thenReturn(Set.of(violation));

        ApiException ex = assertThrows(ApiException.class, () -> authDto.signup(form));

        assertEquals("invalid", ex.getMessage());
        verifyNoInteractions(authApi);
    }

    @Test
    void loginValidCredentials() throws Exception {
        LoginForm form = new LoginForm();
        form.setEmail("  A@B.COM  ");
        form.setPassword("p");

        when(validator.validate(any(LoginForm.class))).thenReturn(Set.of());

        User user = new User();
        user.setId(42);
        user.setEmail("a@b.com");
        user.setRole(UserRole.SUPERVISOR);

        when(authApi.validateLogin("a@b.com", "p")).thenReturn(user);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);

        AuthData out = authDto.login(form, request);

        assertNotNull(out);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(session).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    void changePasswordAuthenticatedUser() throws Exception {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("c");
        form.setNewPassword("n");

        when(validator.validate(any(ChangePasswordForm.class))).thenReturn(Set.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        7,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))
                )
        );

        authDto.changePassword(form);

        verify(authApi).changePassword(7, "c", "n");
    }

    @Test
    void getSessionInfoNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        ApiException ex = assertThrows(ApiException.class, authDto::getSessionInfo);

        assertEquals(ErrorMessages.NOT_LOGGED_IN.value(), ex.getMessage());
        verifyNoInteractions(authApi);
    }
}