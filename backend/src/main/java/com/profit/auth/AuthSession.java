package com.profit.auth;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record AuthSession(
        String token,
        String username,
        String displayName,
        Instant expireAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expireAt);
    }

    public String formattedExpireAt() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(expireAt);
    }

    public CurrentUser toCurrentUser() {
        return new CurrentUser(username, displayName, formattedExpireAt());
    }
}
