package com.pos.dto;

import com.pos.api.UserApi;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import com.pos.utils.TsvParser;
import com.pos.utils.TsvUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.pos.model.constants.ErrorMessages.*;

@Component
public class UserUploadDto extends AbstractDto {

    @Autowired
    private UserApi userApi;

    public void upload(MultipartFile file) throws ApiException, IOException {

        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "email", "role", "password");

        Set<String> seenEmails = new HashSet<>();

        List<User> users = TsvUploadUtil.parseOrThrow(
                rows,
                "user_upload_errors",
                (r, lineNumber) -> {
                    String email = TsvParser.s(r, 0).toLowerCase();
                    String roleStr = TsvParser.s(r, 1).toUpperCase();
                    String password = TsvParser.s(r, 2);

                    if (email.isEmpty()) {
                        throw new ApiException(EMAIL_REQUIRED.value() + " | line=" + lineNumber);
                    }
                    if (!isValidEmail(email)) {
                        throw new ApiException(INVALID_EMAIL.value() + ": " + email + " | line=" + lineNumber);
                    }

                    if (!seenEmails.add(email)) {
                        throw new ApiException(DUPLICATE_EMAIL_IN_FILE.value() + ": " + email + " | line=" + lineNumber);
                    }

                    UserRole role;
                    try {
                        role = UserRole.valueOf(roleStr);
                    } catch (Exception e) {
                        throw new ApiException(INVALID_ROLE.value() + ": " + roleStr + " | email=" + email + " | line=" + lineNumber);
                    }

                    if (password.isEmpty()) {
                        throw new ApiException(PASSWORD_REQUIRED.value() + " | email=" + email + " | line=" + lineNumber);
                    }

                    User user = new User();
                    user.setEmail(email);
                    user.setRole(role);
                    user.setPasswordHash(password);

                    return user;
                }
        );

        userApi.bulkCreateOrUpdate(users);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
