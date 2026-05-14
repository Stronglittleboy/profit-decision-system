package com.profit.auth;

public record LoginResponse(
        String token,
        String username,
        String displayName,
        String expireAt) {
}
