package com.profit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthSessionConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.auth", name = "session-store", havingValue = "redis", matchIfMissing = true)
    public TokenStore redisTokenStore(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        return new RedisTokenStore(stringRedisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.auth", name = "session-store", havingValue = "memory")
    public TokenStore inMemoryTokenStore() {
        return new InMemoryTokenStore();
    }
}
