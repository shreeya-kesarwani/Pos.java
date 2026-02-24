package com.pos.auth.integration.dto;

import com.pos.dao.UserDao;
import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.ErrorMessages;
import com.pos.model.data.AuthData;
import com.pos.model.form.SignupForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoSessionInfoIT extends AbstractAuthIntegrationTest {

    @Autowired AuthDto authDto;
    @Autowired UserDao userDao;

    @Test
    void shouldReturnSessionInfoWhenPrincipalIsStringId() throws Exception {
        SignupForm signup = new SignupForm();
        signup.setEmail("string@id.com");
        signup.setPassword("p");
        authDto.signup(signup);

        Integer userId = userDao.findByEmail("string@id.com").orElseThrow().getId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
                )
        );

        AuthData out = authDto.getSessionInfo();

        assertNotNull(out);
        assertEquals("string@id.com", out.getEmail());
    }

    @Test
    void shouldThrowWhenNotLoggedIn() {
        SecurityContextHolder.clearContext();
        ApiException ex = assertThrows(ApiException.class, authDto::getSessionInfo);
        assertEquals(ErrorMessages.NOT_LOGGED_IN.value(), ex.getMessage());
    }

    @Test
    void shouldThrowWhenPrincipalInvalid() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "not-a-number",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
                )
        );

        ApiException ex = assertThrows(ApiException.class, authDto::getSessionInfo);
        assertEquals(ErrorMessages.INVALID_SESSION_PRINCIPAL.value(), ex.getMessage());
    }

    @Test
    void shouldThrowWhenPrincipalUnexpectedType() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new Object(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
                )
        );

        ApiException ex = assertThrows(ApiException.class, authDto::getSessionInfo);
        assertEquals(ErrorMessages.INVALID_SESSION_PRINCIPAL.value(), ex.getMessage());
    }
}