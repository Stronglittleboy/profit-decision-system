package com.profit.auth;

import com.profit.common.exception.BusinessException;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties authProperties;
    private final TokenStore tokenStore;

    public LoginResponse login(LoginRequest request) {
        if (!Objects.equals(request.username(), authProperties.getUsername())
                || !Objects.equals(request.password(), authProperties.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        Duration ttl = Duration.ofMinutes(authProperties.getTokenTtlMinutes());
        AuthSession session = tokenStore.createSession(
                authProperties.getUsername(),
                authProperties.getDisplayName(),
                ttl);
        return new LoginResponse(
                session.token(),
                session.username(),
                session.displayName(),
                session.formattedExpireAt());
    }

    public CurrentUser currentUser(String token) {
        return tokenStore.findValidSession(token)
                .map(AuthSession::toCurrentUser)
                .orElseThrow(() -> new BusinessException(401, "登录已过期，请重新登录"));
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }
}
