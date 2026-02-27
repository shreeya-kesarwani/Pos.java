package com.pos.userUpload.unit;

import com.pos.api.UserUploadApi;
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

import java.util.List;
import java.util.Optional;

import static com.pos.model.constants.ErrorMessages.INVALID_EMAIL;
import static com.pos.model.constants.ErrorMessages.USER_BULK_EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUploadApiTest {

    @InjectMocks
    private UserUploadApi userUploadApi;

    @Mock
    private UserDao userDao;

    private String existingEmail;
    private String newEmail;

    @BeforeEach
    void setupData() {
        existingEmail = "e@e.com";
        newEmail = "n@n.com";
    }

    private User user(String email, String passwordHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        return u;
    }

    @Test
    void bulkCreateOrUpdateShouldThrowWhenNullList() {
        ApiException ex = assertThrows(ApiException.class, () -> userUploadApi.bulkCreateOrUpdate(null));
        assertEquals(USER_BULK_EMPTY.value(), ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateShouldThrowWhenEmptyList() {
        ApiException ex = assertThrows(ApiException.class, () -> userUploadApi.bulkCreateOrUpdate(List.of()));
        assertEquals(USER_BULK_EMPTY.value(), ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateShouldThrowWhenEmailNull() {
        User incoming = user(null, "pass", UserRole.OPERATOR);

        ApiException ex = assertThrows(ApiException.class,
                () -> userUploadApi.bulkCreateOrUpdate(List.of(incoming)));

        assertEquals(INVALID_EMAIL.value() + ": " + null, ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateShouldThrowWhenEmailBlank() {
        User incoming = user("   ", "pass", UserRole.OPERATOR);

        ApiException ex = assertThrows(ApiException.class,
                () -> userUploadApi.bulkCreateOrUpdate(List.of(incoming)));

        assertEquals(INVALID_EMAIL.value() + ": " + "   ", ex.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateShouldInsertNewUserAndEncodePassword() throws ApiException {
        User incoming = user("a@b.com", "plainPass", UserRole.SUPERVISOR);
        when(userDao.findByEmail("a@b.com")).thenReturn(Optional.empty());

        userUploadApi.bulkCreateOrUpdate(List.of(incoming));

        verify(userDao).findByEmail("a@b.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();

        assertEquals("a@b.com", inserted.getEmail());
        assertEquals(UserRole.SUPERVISOR, inserted.getRole());

        assertNotNull(inserted.getPasswordHash());
        assertNotEquals("plainPass", inserted.getPasswordHash());
        assertTrue(inserted.getPasswordHash().startsWith("$2")); // bcrypt signature

        verifyNoMoreInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateShouldUpdateExistingUserRoleAndNotInsert() throws ApiException {
        User existing = user("x@y.com", "$2b$10$somehash", UserRole.OPERATOR);
        when(userDao.findByEmail("x@y.com")).thenReturn(Optional.of(existing));

        User incoming = user("x@y.com", "ignoredPass", UserRole.SUPERVISOR);

        userUploadApi.bulkCreateOrUpdate(List.of(incoming));

        assertEquals(UserRole.SUPERVISOR, existing.getRole());
        verify(userDao).findByEmail("x@y.com");
        verify(userDao, never()).insert(any());
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void bulkCreateOrUpdateMixedListShouldInsertNewAndUpdateExisting() throws ApiException {
        User existing = user(existingEmail, "$2b$10$hash", UserRole.OPERATOR);

        when(userDao.findByEmail(existingEmail)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail(newEmail)).thenReturn(Optional.empty());

        User incomingExisting = user(existingEmail, "ignored", UserRole.SUPERVISOR);
        User incomingNew = user(newEmail, "plain", UserRole.OPERATOR);

        userUploadApi.bulkCreateOrUpdate(List.of(incomingExisting, incomingNew));

        // updated existing role
        assertEquals(UserRole.SUPERVISOR, existing.getRole());

        verify(userDao).findByEmail(existingEmail);
        verify(userDao).findByEmail(newEmail);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();

        assertEquals(newEmail, inserted.getEmail());
        assertEquals(UserRole.OPERATOR, inserted.getRole());
        assertNotNull(inserted.getPasswordHash());
        assertNotEquals("plain", inserted.getPasswordHash());
        assertTrue(inserted.getPasswordHash().startsWith("$2"));

        verifyNoMoreInteractions(userDao);
    }
}