package com.profit.auth;

import com.profit.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthProperties authProperties;
    private final TokenStore tokenStore;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String remote = clientIp(httpRequest);
        if (!Objects.equals(request.username(), authProperties.getUsername())
                || !Objects.equals(request.password(), authProperties.getPassword())) {
            log.warn("auth login failed remote={} user={}", remote, request.username());
            throw new BusinessException(401, "用户名或密码错误");
        }

        Duration ttl = Duration.ofMinutes(authProperties.getTokenTtlMinutes());
        AuthSession session = tokenStore.createSession(
                authProperties.getUsername(),
                authProperties.getDisplayName(),
                ttl);
        log.info("auth login success remote={} user={}", remote, session.username());
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
        tokenStore.findValidSession(token).ifPresent(s -> log.info("auth logout user={}", s.username()));
        tokenStore.remove(token);
    }

    private static String clientIp(HttpServletRequest req) {
        if (req == null) {
            return "";
        }
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr() == null ? "" : req.getRemoteAddr();
    }
}
