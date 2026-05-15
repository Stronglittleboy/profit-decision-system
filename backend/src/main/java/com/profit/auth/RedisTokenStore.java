package com.profit.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {

    static final String KEY_PREFIX = "profit:auth:session:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AuthSession createSession(String username, String displayName, Duration ttl) {
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthSession session = new AuthSession(token, username, displayName, Instant.now().plus(ttl));
        String key = KEY_PREFIX + token;
        try {
            String json = objectMapper.writeValueAsString(RedisSessionPayload.from(session));
            Duration storeTtl = ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(1) : ttl;
            stringRedisTemplate.opsForValue().set(key, json, storeTtl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法序列化会话", e);
        }
        return session;
    }

    @Override
    public Optional<AuthSession> findValidSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + token;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            RedisSessionPayload payload = objectMapper.readValue(json, RedisSessionPayload.class);
            AuthSession session = payload.toSession();
            if (session.isExpired()) {
                stringRedisTemplate.delete(key);
                return Optional.empty();
            }
            return Optional.of(session);
        } catch (JsonProcessingException e) {
            stringRedisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void remove(String token) {
        if (token != null && !token.isBlank()) {
            stringRedisTemplate.delete(KEY_PREFIX + token);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class RedisSessionPayload {
        String token;
        String username;
        String displayName;
        long expireAtEpochMilli;

        static RedisSessionPayload from(AuthSession s) {
            RedisSessionPayload p = new RedisSessionPayload();
            p.token = s.token();
            p.username = s.username();
            p.displayName = s.displayName();
            p.expireAtEpochMilli = s.expireAt().toEpochMilli();
            return p;
        }

        AuthSession toSession() {
            return new AuthSession(
                    token,
                    username,
                    displayName,
                    Instant.ofEpochMilli(expireAtEpochMilli));
        }
    }
}
