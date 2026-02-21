package com.pos.dto;

import com.pos.api.AuthApi;
import com.pos.exception.ApiException;
import com.pos.model.constants.ErrorMessages;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
import static com.pos.utils.AuthConversion.convertUserToAuthData;
import static com.pos.utils.AuthConversion.convertUserToSignupData;

@Component
public class AuthDto extends AbstractDto {

    @Autowired
    private AuthApi authApi;

    public AuthData signup(SignupForm form) throws ApiException {

        normalize(form);
        validateForm(form);
        User user = authApi.signup(form.getEmail().toLowerCase(), form.getPassword());
        return convertUserToSignupData(user);
    }

    public AuthData login(LoginForm form, HttpServletRequest request) throws ApiException {

        normalize(form);
        validateForm(form);
        User user = authApi.validateLogin(form.getEmail().toLowerCase(), form.getPassword());
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

    public void changePassword(ChangePasswordForm form) throws ApiException {
        normalize(form);
        validateForm(form);
        Integer userId = getRequiredUserId();
        authApi.changePassword(userId, form.getCurrentPassword(), form.getNewPassword());
    }

    public AuthData getSessionInfo() throws ApiException {
        Integer userId = getRequiredUserId();
        User user = authApi.getById(userId);
        return convertUserToAuthData(user);
    }

    private Integer getRequiredUserId() throws ApiException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ApiException(ErrorMessages.NOT_LOGGED_IN.value());
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Integer) return (Integer) principal;
        if (principal instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ApiException(ErrorMessages.INVALID_SESSION_PRINCIPAL.value());
            }
        }
        throw new ApiException(ErrorMessages.INVALID_SESSION_PRINCIPAL.value());
    }
}