package com.pos.unit.api;

import com.pos.api.UserUploadApi;
import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static com.pos.model.constants.ErrorMessages.INVALID_EMAIL;
import static com.pos.model.constants.ErrorMessages.USER_BULK_EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUploadApiTest {

    @InjectMocks
    private UserUploadApi userUploadApi;

    @Mock
    private UserDao userDao;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    private static User user(String email, String rawPasswordOrHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(rawPasswordOrHash);
        u.setRole(role);
        return u;
    }

    @Test
    void bulkCreateOrUpdate_shouldThrow_whenNullList() {
        ApiException ex = assertThrows(ApiException.class, () -> userUploadApi.bulkCreateOrUpdate(null));
        assertEquals(USER_BULK_EMPTY.value(), ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_shouldThrow_whenEmptyList() {
        ApiException ex = assertThrows(ApiException.class, () -> userUploadApi.bulkCreateOrUpdate(List.of()));
        assertEquals(USER_BULK_EMPTY.value(), ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_shouldThrow_whenEmailNull() {
        User incoming = user(null, "pass", UserRole.OPERATOR);

        ApiException ex = assertThrows(ApiException.class,
                () -> userUploadApi.bulkCreateOrUpdate(List.of(incoming)));

        assertEquals(INVALID_EMAIL.value() + ": " + null, ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_shouldThrow_whenEmailBlank() {
        User incoming = user("   ", "pass", UserRole.OPERATOR);

        ApiException ex = assertThrows(ApiException.class,
                () -> userUploadApi.bulkCreateOrUpdate(List.of(incoming)));

        assertEquals(INVALID_EMAIL.value() + ": " + "   ", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_shouldInsertNewUser_andEncodePassword() throws Exception {
        User incoming = user("a@b.com", "plainPass", UserRole.SUPERVISOR);
        when(userDao.findByEmail("a@b.com")).thenReturn(Optional.empty());

        userUploadApi.bulkCreateOrUpdate(List.of(incoming));

        verify(userDao, times(1)).findByEmail("a@b.com");

        // insert(T) is erased -> Mockito sees insert(Object), so capture Object and cast.
        ArgumentCaptor<Object> cap = ArgumentCaptor.forClass(Object.class);
        verify(userDao, times(1)).insert(cap.capture());

        Object insertedObj = cap.getValue();
        assertNotNull(insertedObj);
        assertTrue(insertedObj instanceof User);

        User inserted = (User) insertedObj;
        assertEquals("a@b.com", inserted.getEmail());
        assertEquals(UserRole.SUPERVISOR, inserted.getRole());

        // password must be encoded (bcrypt hashes start with $2a/$2b/$2y)
        assertNotNull(inserted.getPasswordHash());
        assertNotEquals("plainPass", inserted.getPasswordHash());
        assertTrue(inserted.getPasswordHash().startsWith("$2"));

        verifyNoMoreInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_shouldUpdateExistingUserRole_andNotInsert() throws Exception {
        User existing = user("x@y.com", "$2b$10$somehash", UserRole.OPERATOR);
        when(userDao.findByEmail("x@y.com")).thenReturn(Optional.of(existing));

        User incoming = user("x@y.com", "ignoredPass", UserRole.SUPERVISOR);

        userUploadApi.bulkCreateOrUpdate(List.of(incoming));

        assertEquals(UserRole.SUPERVISOR, existing.getRole());
        verify(userDao, times(1)).findByEmail("x@y.com");
        verify(userDao, never()).insert(any());
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdate_mixedList_shouldInsertNew_andUpdateExisting() throws Exception {
        User existing = user("e@e.com", "$2b$10$hash", UserRole.OPERATOR);
        when(userDao.findByEmail("e@e.com")).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("n@n.com")).thenReturn(Optional.empty());

        User incomingExisting = user("e@e.com", "ignored", UserRole.SUPERVISOR);
        User incomingNew = user("n@n.com", "plain", UserRole.OPERATOR);

        userUploadApi.bulkCreateOrUpdate(List.of(incomingExisting, incomingNew));

        assertEquals(UserRole.SUPERVISOR, existing.getRole());

        verify(userDao).findByEmail("e@e.com");
        verify(userDao).findByEmail("n@n.com");

        // verify insert called for new user; cast inside matcher because param is Object
        verify(userDao, times(1)).insert(argThat(o -> {
            if (!(o instanceof User u)) return false;
            return "n@n.com".equals(u.getEmail())
                    && u.getPasswordHash() != null
                    && !u.getPasswordHash().equals("plain")
                    && u.getPasswordHash().startsWith("$2");
        }));

        verifyNoMoreInteractions(userDao);
    }
}
