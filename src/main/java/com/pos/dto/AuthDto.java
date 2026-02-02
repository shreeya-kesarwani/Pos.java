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

@Component
public class AuthDto extends AbstractDto {

    @Autowired
    private AuthApi authApi;

    public AuthData signup(SignupForm form) throws ApiException {
        normalize(form);
        String email = normalizeEmailLowercase(form.getEmail());
        String password = form.getPassword(); // keep as-is (do NOT lowercase)

        User user = authApi.signup(email, password);

        AuthData data = new AuthData();
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        data.setToken(null);
        return data;
    }

    public AuthData login(LoginForm form) throws ApiException {
        normalize(form);

        String email = normalizeEmailLowercase(form.getEmail());
        String password = form.getPassword();

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
            throw new ApiException("Email cannot be empty");
        }
        return email.trim().toLowerCase();
    }
}
