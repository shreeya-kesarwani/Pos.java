package com.pos.auth.integration.dto;

import com.pos.dao.UserDao;
import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.model.data.AuthData;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoSignupIT extends AbstractAuthIntegrationTest {

    @Autowired AuthDto authDto;
    @Autowired UserDao userDao;

    @Test
    void shouldSignupAndPersistUser_happyFlow() throws Exception {
        SignupForm form = new SignupForm();
        form.setEmail("  TEST@EXAMPLE.COM  ");
        form.setPassword("pass123");

        AuthData out = authDto.signup(form);

        assertNotNull(out);

        Optional<User> saved = userDao.findByEmail("test@example.com");
        assertTrue(saved.isPresent());
        assertEquals("test@example.com", saved.get().getEmail());
        assertEquals(UserRole.OPERATOR, saved.get().getRole());
        assertNotNull(saved.get().getPasswordHash());
    }

    @Test
    void shouldThrowWhenEmailInvalid() {
        SignupForm form = new SignupForm();
        form.setEmail("not-an-email");
        form.setPassword("pass");

        assertThrows(ApiException.class, () -> authDto.signup(form));
    }

    @Test
    void shouldThrowWhenPasswordBlank() {
        SignupForm form = new SignupForm();
        form.setEmail("a@b.com");
        form.setPassword("   ");

        assertThrows(ApiException.class, () -> authDto.signup(form));
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() throws Exception {
        SignupForm form = new SignupForm();
        form.setEmail("dup@x.com");
        form.setPassword("p");

        authDto.signup(form);

        SignupForm again = new SignupForm();
        again.setEmail("dup@x.com");
        again.setPassword("p2");

        ApiException ex = assertThrows(ApiException.class, () -> authDto.signup(again));
        assertNotNull(ex.getMessage());
    }
}