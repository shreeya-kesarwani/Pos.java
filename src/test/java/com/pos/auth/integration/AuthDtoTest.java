package com.pos.auth.integration;

import com.pos.dao.UserDao;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthDtoTest {

    @Autowired private AuthDto authDto;
    @Autowired private UserDao userDao;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signupTrimsAndLowercasesEmailAndPersistsUser() throws Exception {
        SignupForm form = new SignupForm();
        form.setEmail("  TEST@EXAMPLE.COM  ");
        form.setPassword("pass123");

        AuthData out = authDto.signup(form);

        assertNotNull(out);

        Optional<User> saved = userDao.findByEmail("test@example.com");
        assertTrue(saved.isPresent());
        assertEquals("test@example.com", saved.get().getEmail());
        assertEquals(UserRole.OPERATOR, saved.get().getRole()); // based on your AuthApiTest expectation
        assertNotNull(saved.get().getPasswordHash());
    }

    @Test
    void signupThrowsWhenEmailInvalid() {
        SignupForm form = new SignupForm();
        form.setEmail("not-an-email");
        form.setPassword("pass");

        assertThrows(ApiException.class, () -> authDto.signup(form));
    }

    @Test
    void signupThrowsWhenPasswordBlank() {
        SignupForm form = new SignupForm();
        form.setEmail("a@b.com");
        form.setPassword("   ");

        assertThrows(ApiException.class, () -> authDto.signup(form));
    }

    @Test
    void loginSetsSecurityContextAndSession() throws Exception {
        // Arrange: create user via real signup so login can succeed
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

        // Act
        AuthData out = authDto.login(form, request);

        // Assert
        assertNotNull(out);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        verify(session).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    void loginThrowsWhenCredentialsInvalid() {
        LoginForm form = new LoginForm();
        form.setEmail("missing@b.com");
        form.setPassword("wrong");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));

        assertThrows(ApiException.class, () -> authDto.login(form, request));
    }

    @Test
    void changePasswordCallsApiForAuthenticatedUser() throws Exception {
        // Arrange: create user
        SignupForm signup = new SignupForm();
        signup.setEmail("x@y.com");
        signup.setPassword("oldPass");
        authDto.signup(signup);

        Integer userId = userDao.findByEmail("x@y.com").orElseThrow().getId();
        assertNotNull(userId);

        // Put userId as principal (matches how your existing test sets principal = 7)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))
                )
        );

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("oldPass");
        form.setNewPassword("newPass");

        // Act
        authDto.changePassword(form);

        // Assert: verify login works with new password
        LoginForm login = new LoginForm();
        login.setEmail("x@y.com");
        login.setPassword("newPass");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));

        AuthData out = authDto.login(login, request);
        assertNotNull(out);
    }

    @Test
    void changePasswordThrowsWhenNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("c");
        form.setNewPassword("n");

        assertThrows(ApiException.class, () -> authDto.changePassword(form));
    }

    @Test
    void getSessionInfoThrowsWhenNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        ApiException ex = assertThrows(ApiException.class, authDto::getSessionInfo);
        assertEquals(ErrorMessages.NOT_LOGGED_IN.value(), ex.getMessage());
    }
}