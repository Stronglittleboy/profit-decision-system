package com.profit.auth;

public record CurrentUser(
        String username,
        String displayName,
        String expireAt) {
}
