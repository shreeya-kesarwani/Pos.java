package com.pos.dto;

import com.pos.api.AuthApi;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.EMAIL_CANNOT_BE_EMPTY;
import static com.pos.model.constants.ErrorMessages.PASSWORD_CANNOT_BE_EMPTY;
import static com.pos.utils.AuthConversion.convertUserToAuthData;
import static com.pos.utils.AuthConversion.convertUserToSignupData;

@Component
public class AuthDto extends AbstractDto {

    @Autowired
    private AuthApi authApi;

    public AuthData signup(SignupForm form) throws ApiException {
        normalize(form);

        String email = normalizeEmailLowercase(form.getEmail());
        String password = normalizePasswordRequired(form.getPassword());

        User user = authApi.signup(email, password);
        return convertUserToSignupData(user);
    }

    public AuthData login(LoginForm form, HttpServletRequest request) throws ApiException {
        normalize(form);

        String email = normalizeEmailLowercase(form.getEmail());
        String password = normalizePasswordRequired(form.getPassword());

        User user = authApi.validateLogin(email, password);

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                List.of(authority)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return convertUserToAuthData(user);
    }

    public void changePassword(@Valid ChangePasswordForm form) throws ApiException {
        normalize(form);
        Integer userId = requireLoggedInUserId();

        String currentPassword = normalizePasswordRequired(form.getCurrentPassword());
        String newPassword = normalizePasswordRequired(form.getNewPassword());

        authApi.changePassword(userId, currentPassword, newPassword);
    }

    public AuthData getSessionInfo() throws ApiException {
        Integer userId = requireLoggedInUserId();
        User user = authApi.getById(userId);
        return convertUserToAuthData(user);
    }

    private Integer requireLoggedInUserId() throws ApiException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ApiException("You are not logged in");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof Integer) return (Integer) principal;

        if (principal instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid session principal");
            }
        }

        throw new ApiException("Invalid session principal");
    }

    private String normalizeEmailLowercase(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException(EMAIL_CANNOT_BE_EMPTY.value());
        }
        return email.trim().toLowerCase();
    }

    private String normalizePasswordRequired(String password) throws ApiException {
        if (!StringUtils.hasText(password)) {
            throw new ApiException(PASSWORD_CANNOT_BE_EMPTY.value());
        }
        return password.trim();
    }
}
