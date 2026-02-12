package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;

import java.util.HashSet;
import java.util.Set;

import static com.pos.model.constants.ErrorMessages.*;

public class UserTsvUploadUtil {

    private UserTsvUploadUtil() {}

    // Keep regex here so DTO doesn't carry it around
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static Set<String> newSeenEmails() {
        return new HashSet<>();
    }

    public static User parseRowOrThrow(String[] r, int lineNumber, Set<String> seenEmails) throws ApiException {
        String email = TsvParser.s(r, 0).toLowerCase();
        String roleStr = TsvParser.s(r, 1).toUpperCase();
        String password = TsvParser.s(r, 2);

        requireNonEmpty(email, EMAIL_REQUIRED.value(), lineNumber, null);
        if (!isValidEmail(email)) {
            throw new ApiException(INVALID_EMAIL.value() + ": " + email + " | line=" + lineNumber);
        }

        if (!seenEmails.add(email)) {
            throw new ApiException(DUPLICATE_EMAIL_IN_FILE.value() + ": " + email + " | line=" + lineNumber);
        }

        UserRole role = parseRole(roleStr, email, lineNumber);

        requireNonEmpty(password, PASSWORD_REQUIRED.value(), lineNumber, email);

        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        // NOTE: ideally this should be hashed later; keeping as-is to match your current flow
        user.setPasswordHash(password);

        return user;
    }

    private static UserRole parseRole(String roleStr, String email, int lineNumber) throws ApiException {
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            throw new ApiException(INVALID_ROLE.value() + ": " + roleStr + " | email=" + email + " | line=" + lineNumber);
        }
    }

    private static void requireNonEmpty(String value, String msg, int lineNumber, String emailOrNull) throws ApiException {
        if (value == null || value.isEmpty()) {
            if (emailOrNull == null) {
                throw new ApiException(msg + " | line=" + lineNumber);
            }
            throw new ApiException(msg + " | email=" + emailOrNull + " | line=" + lineNumber);
        }
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
}
