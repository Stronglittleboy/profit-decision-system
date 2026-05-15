package com.profit.auth;

import java.time.Duration;
import java.util.Optional;

public interface TokenStore {

    AuthSession createSession(String username, String displayName, Duration ttl);

    Optional<AuthSession> findValidSession(String token);

    void remove(String token);
}
