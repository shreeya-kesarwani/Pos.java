package com.pos.unit.api;

import com.pos.api.AuthApi;
import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthApiTest {

    @InjectMocks
    private AuthApi authApi;

    @Mock
    private UserDao userDao;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_shouldCreateOperator_andInsertUser_whenEmailNotRegistered() throws Exception {
        String email = "a@b.com";
        String password = "pass123";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        User created = authApi.signup(email, password);

        // Insert called with a populated user
        verify(userDao).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();

        assertNotNull(created);
        assertSame(inserted, created, "AuthApi returns the same user instance it inserts");

        assertEquals(email, inserted.getEmail());
        assertEquals(UserRole.OPERATOR, inserted.getRole());

        // password should be hashed (not equal raw), and should validate with BCrypt
        assertNotNull(inserted.getPasswordHash());
        assertNotEquals(password, inserted.getPasswordHash());

        // validate login using returned hash by stubbing DAO
        when(userDao.findByEmail(email)).thenReturn(Optional.of(inserted));
        User loggedIn = authApi.validateLogin(email, password);
        assertEquals(email, loggedIn.getEmail());
    }

    @Test
    void signup_shouldThrow_whenEmailAlreadyRegistered() {
        String email = "exists@b.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.of(new User()));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.signup(email, "x"));
        assertTrue(ex.getMessage().contains(EMAIL_ALREADY_REGISTERED.value()));
        assertTrue(ex.getMessage().contains(email));

        verify(userDao, never()).insert(any());
    }

    @Test
    void validateLogin_shouldReturnUser_whenCredentialsCorrect() throws Exception {
        String email = "u@b.com";
        String password = "secret";

        User u = new User();
        u.setEmail(email);
        // Use the same BCrypt inside AuthApi by calling signup-style encode indirectly:
        // easiest: set hash by calling AuthApi.signup with mocked dao insert, but here we just reuse encoder via reflection.
        // We'll create a hash by invoking AuthApi.signup flow partially:
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User temp = authApi.signup(email, password);

        // now test validateLogin
        when(userDao.findByEmail(email)).thenReturn(Optional.of(temp));
        User out = authApi.validateLogin(email, password);

        assertNotNull(out);
        assertEquals(email, out.getEmail());
    }

    @Test
    void validateLogin_shouldThrow_whenUserNotFound() {
        String email = "missing@b.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "x"));
        assertTrue(ex.getMessage().contains(INVALID_CREDENTIALS.value()));
        assertTrue(ex.getMessage().contains(email));
    }

    @Test
    void validateLogin_shouldThrow_whenPasswordIncorrect() throws Exception {
        String email = "u2@b.com";

        // Create a user with a known hash (via signup)
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        User u = authApi.signup(email, "correct");

        when(userDao.findByEmail(email)).thenReturn(Optional.of(u));

        ApiException ex = assertThrows(ApiException.class, () -> authApi.validateLogin(email, "wrong"));
        assertEquals(INVALID_CREDENTIALS.value(), ex.getMessage());
    }

    @Test
    void getById_shouldReturnUser_whenExists() throws Exception {
        User u = new User();
        u.setId(10);
        when(userDao.selectById(10)).thenReturn(u);

        User out = authApi.getById(10);
        assertSame(u, out);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(userDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.getById(99));
        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void changePassword_shouldUpdateHash_whenCurrentPasswordMatches() throws Exception {
        int userId = 1;
        String current = "oldPass";
        String next = "newPass";

        // Create a user with current password hash
        when(userDao.findByEmail("tmp@b.com")).thenReturn(Optional.empty());
        User u = authApi.signup("tmp@b.com", current);
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        String oldHash = u.getPasswordHash();
        authApi.changePassword(userId, current, next);

        assertNotEquals(oldHash, u.getPasswordHash(), "hash should be changed");
        assertNotEquals(next, u.getPasswordHash(), "should not store raw password");

        // verify new password works
        when(userDao.findByEmail("tmp@b.com")).thenReturn(Optional.of(u));
        User out = authApi.validateLogin("tmp@b.com", next);
        assertEquals("tmp@b.com", out.getEmail());
    }

    @Test
    void changePassword_shouldThrow_whenUserNotFound() {
        when(userDao.selectById(5)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(5, "a", "b"));
        assertTrue(ex.getMessage().contains(USER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordIncorrect() throws Exception {
        int userId = 2;

        when(userDao.findByEmail("t2@b.com")).thenReturn(Optional.empty());
        User u = authApi.signup("t2@b.com", "right");
        u.setId(userId);

        when(userDao.selectById(userId)).thenReturn(u);

        ApiException ex = assertThrows(ApiException.class, () -> authApi.changePassword(userId, "wrong", "new"));
        assertEquals(CURRENT_PASSWORD_INCORRECT.value(), ex.getMessage());
    }
}
