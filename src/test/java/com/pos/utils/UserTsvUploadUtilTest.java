package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import com.pos.utils.TsvParser;
import com.pos.utils.UserTsvUploadUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class UserTsvUploadUtilTest {

    @Test
    void newSeenEmails_shouldReturnEmptySet() {
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();
        assertNotNull(seen);
        assertTrue(seen.isEmpty());
    }

    @Test
    void parseRowOrThrow_shouldCreateUser_whenValid() throws Exception {
        String[] row = {"  TeSt@Example.com  ", " supervisor ", "  pass123  "};
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            // simulate real trimming behavior
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            User u = UserTsvUploadUtil.parseRowOrThrow(row, 7, seen);

            assertEquals("test@example.com", u.getEmail());           // lowercased
            assertEquals(UserRole.SUPERVISOR, u.getRole());           // uppercased then enum
            assertEquals("pass123", u.getPasswordHash());             // stored as-is
            assertTrue(seen.contains("test@example.com"));

            // verify index usage
            mocked.verify(() -> TsvParser.s(row, 0));
            mocked.verify(() -> TsvParser.s(row, 1));
            mocked.verify(() -> TsvParser.s(row, 2));
        }
    }

    @Test
    void parseRowOrThrow_shouldThrow_whenEmailMissing() throws Exception {
        String[] row = {"", "SUPERVISOR", "pass"};
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            ApiException ex = assertThrows(ApiException.class,
                    () -> UserTsvUploadUtil.parseRowOrThrow(row, 2, seen));

            assertTrue(ex.getMessage().contains("line=2"));
        }
    }

    @Test
    void parseRowOrThrow_shouldThrow_whenEmailInvalid() throws Exception {
        String[] row = {"not-an-email", "SUPERVISOR", "pass"};
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            ApiException ex = assertThrows(ApiException.class,
                    () -> UserTsvUploadUtil.parseRowOrThrow(row, 9, seen));

            assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
            assertTrue(ex.getMessage().contains("line=9"));
        }
    }

    @Test
    void parseRowOrThrow_shouldThrow_whenDuplicateEmailInFile() throws Exception {
        String[] row1 = {"a@b.com", "SUPERVISOR", "p1"};
        String[] row2 = {"A@B.COM", "OPERATOR", "p2"}; // same after lowercase
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            UserTsvUploadUtil.parseRowOrThrow(row1, 2, seen);

            ApiException ex = assertThrows(ApiException.class,
                    () -> UserTsvUploadUtil.parseRowOrThrow(row2, 3, seen));

            assertTrue(ex.getMessage().toLowerCase().contains("duplicate"));
            assertTrue(ex.getMessage().contains("line=3"));
        }
    }

    @Test
    void parseRowOrThrow_shouldThrow_whenRoleInvalid() throws Exception {
        String[] row = {"x@y.com", "NOT_A_ROLE", "pass"};
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            ApiException ex = assertThrows(ApiException.class,
                    () -> UserTsvUploadUtil.parseRowOrThrow(row, 5, seen));

            assertTrue(ex.getMessage().toLowerCase().contains("role"));
            assertTrue(ex.getMessage().contains("email=x@y.com"));
            assertTrue(ex.getMessage().contains("line=5"));
        }
    }

    @Test
    void parseRowOrThrow_shouldThrow_whenPasswordMissing() throws Exception {
        String[] row = {"x@y.com", "SUPERVISOR", ""};
        Set<String> seen = UserTsvUploadUtil.newSeenEmails();

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });

            ApiException ex = assertThrows(ApiException.class,
                    () -> UserTsvUploadUtil.parseRowOrThrow(row, 11, seen));

            assertTrue(ex.getMessage().toLowerCase().contains("password"));
            assertTrue(ex.getMessage().contains("email=x@y.com"));
            assertTrue(ex.getMessage().contains("line=11"));
        }
    }
}
