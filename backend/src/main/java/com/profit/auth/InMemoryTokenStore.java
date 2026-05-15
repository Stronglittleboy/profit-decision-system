package com.profit.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenStore implements TokenStore {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

    @Override
    public AuthSession createSession(String username, String displayName, Duration ttl) {
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthSession session = new AuthSession(token, username, displayName, Instant.now().plus(ttl));
        sessions.put(token, session);
        return session;
    }

    @Override
    public Optional<AuthSession> findValidSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        AuthSession session = sessions.get(token);
        if (session == null || session.isExpired()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public void remove(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }
}
