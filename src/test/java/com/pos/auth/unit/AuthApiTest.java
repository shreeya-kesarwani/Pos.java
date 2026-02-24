package com.pos.auth.unit;

import com.pos.api.AuthApi;
import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.junit.jupiter.api.BeforeEach;
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

    private String email;
    private String password;

    @BeforeEach
    void setupData() {
        email = "a@b.com";
        password = "pass123";
    }

    // ---------- Helpers (unit-test safe) ----------

    private User signupUser(String email, String rawPassword) throws ApiException {
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        return authApi.signup(email, rawPassword);
    }

    private void stubUserFoundByEmail(String email, User user) {
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
    }

    // ---------- Tests ----------

    @Test
    void signupInsertsUserWithOperatorRoleAndHashedPassword() throws Exception {
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
    }

    @Test
    void signupThrowsWhenEmailAlreadyRegistered() {
        String existingEmail = "exists@b.com";
        when(userDao.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.signup(existingEmail, "x"));

        assertTrue(ex.getMessage().contains(EMAIL_ALREADY_REGISTERED.value()));
        assertTrue(ex.getMessage().contains(existingEmail));
        verify(userDao, never()).insert(any());
    }

    @Test
    void validateLoginReturnsUserWhenCredentialsCorrect() throws Exception {
        User signedUp = signupUser(email, password);
        stubUserFoundByEmail(email, signedUp);

        User out = authApi.validateLogin(email, password);

        assertNotNull(out);
        assertEquals(email, out.getEmail());
    }

    @Test
    void validateLoginThrowsWhenUserNotFound() {
        String missingEmail = "missing@b.com";
        when(userDao.findByEmail(missingEmail)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(missingEmail, "x"));

        assertTrue(ex.getMessage().contains(INVALID_CREDENTIALS.value()));
        assertTrue(ex.getMessage().contains(missingEmail));
    }

    @Test
    void validateLoginThrowsWhenPasswordIncorrect() throws Exception {
        String raw = "correct";
        User u = signupUser(email, raw);
        stubUserFoundByEmail(email, u);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "wrong"));
        assertEquals(INVALID_CREDENTIALS.value(), ex.getMessage());
    }

    @Test
    void getByIdReturnsUserWhenExists() throws Exception {
        User u = new User();
        u.setId(10);
        when(userDao.selectById(10)).thenReturn(u);

        User out = authApi.getById(10);

        assertSame(u, out);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(userDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.getById(99));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void changePasswordUpdatesHashWhenCurrentPasswordMatches() throws Exception {
        int userId = 1;
        String current = "oldPass";
        String next = "newPass";

        User u = signupUser(email, current);
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        String oldHash = u.getPasswordHash();

        authApi.changePassword(userId, current, next);

        assertNotEquals(oldHash, u.getPasswordHash());
        assertNotEquals(next, u.getPasswordHash());
    }

    @Test
    void changePasswordThrowsWhenUserNotFound() {
        when(userDao.selectById(5)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(5, "a", "b"));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    void changePasswordThrowsWhenCurrentPasswordIncorrect() throws Exception {
        int userId = 2;

        User u = signupUser(email, "right");
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(userId, "wrong", "new"));
        assertEquals(CURRENT_PASSWORD_INCORRECT.value(), ex.getMessage());
    }
}