package com.profit.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenResolver {

    public String resolve(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return header.substring(AuthConstants.TOKEN_PREFIX.length()).trim();
        }
        String token = request.getHeader("X-Token");
        if (token != null && !token.isBlank()) {
            return token.trim();
        }
        return null;
    }
}
