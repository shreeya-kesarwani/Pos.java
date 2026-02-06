package com.pos.dto;

import com.pos.api.AuthApi;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import com.pos.security.AuthPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.pos.model.constants.ErrorMessages.EMAIL_CANNOT_BE_EMPTY;
import static com.pos.model.constants.ErrorMessages.PASSWORD_CANNOT_BE_EMPTY;
import static com.pos.utils.AuthConversion.convertUserToSignupData;

@Component
public class AuthDto extends AbstractDto {

    @Autowired
    private AuthApi authApi;

    public AuthData signup(SignupForm form) throws ApiException {
        normalize(form);

        String email = normalizeEmailLowercase(form.getEmail());
        String password = form.getPassword();
        validatePassword(password);

        User user = authApi.signup(email, password);

        return convertUserToSignupData(user);
    }

    public AuthData login(LoginForm form) throws ApiException {
        normalize(form);

        String email = normalizeEmailLowercase(form.getEmail());
        String password = form.getPassword();
        validatePassword(password);

        return authApi.login(email, password);
    }

    public void changePassword(ChangePasswordForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        AuthPrincipal principal =
                (AuthPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        authApi.changePassword(
                principal.getUserId(),
                form.getCurrentPassword(),
                form.getNewPassword()
        );
    }

    private String normalizeEmailLowercase(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException(EMAIL_CANNOT_BE_EMPTY.value() + ": " + email);
        }
        return email.trim().toLowerCase();
    }

    private void validatePassword(String password) throws ApiException {
        if (password == null || password.trim().isEmpty()) {
            throw new ApiException(PASSWORD_CANNOT_BE_EMPTY.value());
        }
    }
}
