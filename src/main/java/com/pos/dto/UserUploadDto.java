package com.pos.dto;

import com.pos.api.UserApi;
import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.pojo.User;
import com.pos.pojo.UserRole;
import com.pos.utils.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class UserUploadDto extends AbstractDto {

    @Autowired
    private UserApi userApi;

    public void upload(MultipartFile file) throws ApiException, IOException {
        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "email", "role", "password");

        List<String> errors = new ArrayList<>();
        List<User> toCreateOrUpdate = new ArrayList<>();

        Set<String> seenEmails = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                String email = TsvParser.s(r, 0).toLowerCase();
                String roleStr = TsvParser.s(r, 1).toUpperCase();
                String password = TsvParser.s(r, 2);

                if (email.isEmpty()) throw new ApiException("email is required");
                if (!isValidEmail(email)) throw new ApiException("invalid email");

                if (!seenEmails.add(email)) {
                    throw new ApiException("duplicate email in file: " + email);
                }

                UserRole role;
                try {
                    role = UserRole.valueOf(roleStr);
                } catch (Exception e) {
                    throw new ApiException("invalid role");
                }

                if (password.isEmpty()) throw new ApiException("password is required");

                User user = new User();
                user.setEmail(email);
                user.setRole(role);
                user.setPasswordHash(password); // hashed later
                toCreateOrUpdate.add(user);

            } catch (ApiException ex) {
                err = "Line " + (i + 1) + ": " + ex.getMessage();
            } catch (Exception ex) {
                err = "Line " + (i + 1) + ": Invalid row";
            }

            errors.add(err);
        }

        boolean hasAnyError = errors.stream().anyMatch(Objects::nonNull);
        if (hasAnyError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "user_upload_errors_" +
                    LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        // All-or-nothing DB writes
        userApi.bulkCreateOrUpdate(toCreateOrUpdate);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
