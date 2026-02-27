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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private String email;
    private String password;

    @BeforeEach
    void setupData() {
        email = "a@b.com";
        password = "pass123";
    }

    private User userWithHash(Integer id, String email, String rawPassword, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        u.setPasswordHash(encoder.encode(rawPassword));
        return u;
    }

    @Test
    void signupInsertsUserWithOperatorRoleAndHashedPassword() throws Exception {
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        User created = authApi.signup(email, password);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();

        assertNotNull(created);
        assertSame(inserted, created);

        assertEquals(email, inserted.getEmail());
        assertEquals(UserRole.OPERATOR, inserted.getRole());
        assertNotNull(inserted.getPasswordHash());
        assertNotEquals(password, inserted.getPasswordHash());
        assertTrue(encoder.matches(password, inserted.getPasswordHash()));
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
        User u = userWithHash(10, email, password, UserRole.OPERATOR);
        when(userDao.findByEmail(email)).thenReturn(Optional.of(u));

        User out = authApi.validateLogin(email, password);

        assertSame(u, out);
        verify(userDao).findByEmail(email);
        verify(userDao, never()).insert(any());
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void validateLoginThrowsWhenUserNotFound() {
        String missingEmail = "missing@b.com";
        when(userDao.findByEmail(missingEmail)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(missingEmail, "x"));

        assertTrue(ex.getMessage().contains(INVALID_CREDENTIALS.value()));
        assertTrue(ex.getMessage().contains(missingEmail));
        verify(userDao).findByEmail(missingEmail);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void validateLoginThrowsWhenPasswordIncorrect() {
        User u = userWithHash(10, email, "correct", UserRole.OPERATOR);
        when(userDao.findByEmail(email)).thenReturn(Optional.of(u));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "wrong"));
        assertEquals(INVALID_CREDENTIALS.value(), ex.getMessage());

        verify(userDao).findByEmail(email);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void getByIdReturnsUserWhenExists() throws Exception {
        User u = new User();
        u.setId(10);
        when(userDao.selectById(10)).thenReturn(u);

        User out = authApi.getById(10);

        assertSame(u, out);
        verify(userDao).selectById(10);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(userDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.getById(99));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));

        verify(userDao).selectById(99);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void changePasswordUpdatesHashWhenCurrentPasswordMatches() throws Exception {
        int userId = 1;
        String current = "oldPass";
        String next = "newPass";

        User u = userWithHash(userId, email, current, UserRole.OPERATOR);
        when(userDao.selectById(userId)).thenReturn(u);

        String oldHash = u.getPasswordHash();

        authApi.changePassword(userId, current, next);

        assertNotEquals(oldHash, u.getPasswordHash());
        assertTrue(encoder.matches(next, u.getPasswordHash()));
        assertFalse(encoder.matches(current, u.getPasswordHash()));

        verify(userDao).selectById(userId);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void changePasswordThrowsWhenUserNotFound() {
        when(userDao.selectById(5)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(5, "a", "b"));

        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("5"));

        verify(userDao).selectById(5);
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void changePasswordThrowsWhenCurrentPasswordIncorrect() {
        int userId = 2;
        User u = userWithHash(userId, email, "right", UserRole.OPERATOR);
        when(userDao.selectById(userId)).thenReturn(u);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(userId, "wrong", "new"));
        assertEquals(CURRENT_PASSWORD_INCORRECT.value(), ex.getMessage());

        verify(userDao).selectById(userId);
        verifyNoMoreInteractions(userDao);
    }
}