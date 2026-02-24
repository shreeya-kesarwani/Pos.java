package com.pos.auth.integration.dto;

import com.pos.dao.UserDao;
import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthDtoChangePasswordIT extends AbstractAuthIntegrationTest {

    @Autowired AuthDto authDto;
    @Autowired UserDao userDao;

    @Test
    void shouldChangePasswordForAuthenticatedUser_happyFlow() throws Exception {
        SignupForm signup = new SignupForm();
        signup.setEmail("x@y.com");
        signup.setPassword("oldPass");
        authDto.signup(signup);

        Integer userId = userDao.findByEmail("x@y.com").orElseThrow().getId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
                )
        );

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("oldPass");
        form.setNewPassword("newPass");

        authDto.changePassword(form);

        // verify new password works
        LoginForm login = new LoginForm();
        login.setEmail("x@y.com");
        login.setPassword("newPass");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(jakarta.servlet.http.HttpSession.class));

        assertNotNull(authDto.login(login, request));
    }

    @Test
    void shouldThrowWhenNotLoggedIn() {
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
}