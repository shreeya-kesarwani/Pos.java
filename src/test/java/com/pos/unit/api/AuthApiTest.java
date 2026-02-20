package com.pos.unit.api;

import com.pos.api.AuthApi;
import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApiTest {

    @InjectMocks
    private AuthApi authApi;

    @Mock
    private UserDao userDao;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void signupShouldCreateOperatorAndInsertUserWhenEmailNotRegistered() throws Exception {
        String email = "a@b.com";
        String password = "pass123";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        User created = authApi.signup(email, password);

        verify(userDao).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();

        assertNotNull(created);
        assertSame(inserted, created);

        assertEquals(email, inserted.getEmail());
        assertEquals(UserRole.OPERATOR, inserted.getRole());

        assertNotNull(inserted.getPasswordHash());
        assertNotEquals(password, inserted.getPasswordHash());

        when(userDao.findByEmail(email)).thenReturn(Optional.of(inserted));
        User loggedIn = authApi.validateLogin(email, password);
        assertEquals(email, loggedIn.getEmail());
    }

    @Test
    void signupShouldThrowWhenEmailAlreadyRegistered() {
        String email = "exists@b.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.of(new User()));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.signup(email, "x"));

        assertTrue(ex.getMessage().contains(EMAIL_ALREADY_REGISTERED.value()));
        assertTrue(ex.getMessage().contains(email));
        verify(userDao, never()).insert(any());
    }

    @Test
    void validateLoginShouldReturnUserWhenCredentialsCorrect() throws Exception {
        String email = "u@b.com";
        String password = "secret";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User signedUp = authApi.signup(email, password);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(signedUp));
        User out = authApi.validateLogin(email, password);

        assertNotNull(out);
        assertEquals(email, out.getEmail());
    }

    @Test
    void validateLoginShouldThrowWhenUserNotFound() {
        String email = "missing@b.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "x"));

        assertTrue(ex.getMessage().contains(INVALID_CREDENTIALS.value()));
        assertTrue(ex.getMessage().contains(email));
    }

    @Test
    void validateLoginShouldThrowWhenPasswordIncorrect() throws Exception {
        String email = "u2@b.com";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User u = authApi.signup(email, "correct");

        when(userDao.findByEmail(email)).thenReturn(Optional.of(u));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "wrong"));
        assertEquals(INVALID_CREDENTIALS.value(), ex.getMessage());
    }

    @Test
    void getByIdShouldReturnUserWhenExists() throws Exception {
        User u = new User();
        u.setId(10);
        when(userDao.selectById(10)).thenReturn(u);

        User out = authApi.getById(10);
        assertSame(u, out);
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        when(userDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.getById(99));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void changePasswordShouldUpdateHashWhenCurrentPasswordMatches() throws Exception {
        int userId = 1;
        String current = "oldPass";
        String next = "newPass";
        String email = "tmp@b.com";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User u = authApi.signup(email, current);
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        String oldHash = u.getPasswordHash();
        authApi.changePassword(userId, current, next);

        assertNotEquals(oldHash, u.getPasswordHash());
        assertNotEquals(next, u.getPasswordHash());

        when(userDao.findByEmail(email)).thenReturn(Optional.of(u));
        User out = authApi.validateLogin(email, next);
        assertEquals(email, out.getEmail());
    }

    @Test
    void changePasswordShouldThrowWhenUserNotFound() {
        when(userDao.selectById(5)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(5, "a", "b"));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIncorrect() throws Exception {
        int userId = 2;
        String email = "t2@b.com";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User u = authApi.signup(email, "right");
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(userId, "wrong", "new"));
        assertEquals(CURRENT_PASSWORD_INCORRECT.value(), ex.getMessage());
    }
}