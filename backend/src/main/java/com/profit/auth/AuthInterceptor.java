package com.profit.auth;

import com.profit.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;
    private final AuthTokenResolver tokenResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/health")
                || uri.startsWith("/actuator")
                || uri.startsWith("/error")) {
            return true;
        }

        String token = tokenResolver.resolve(request);
        AuthSession session = tokenStore.findValidSession(token)
                .orElseThrow(() -> new BusinessException(401, "未登录或登录已过期"));
        request.setAttribute(AuthConstants.REQUEST_USER_ATTR, session.toCurrentUser());
        request.setAttribute(AuthConstants.AUTH_HEADER, token);
        return true;
    }
}
