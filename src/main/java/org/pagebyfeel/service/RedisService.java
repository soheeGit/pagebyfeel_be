package org.pagebyfeel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void saveRefreshToken(UUID userId, String refreshToken, long expirationDays) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofDays(expirationDays)
        );
        log.info("Refresh token saved for user: {}", userId);
    }

    public String getRefreshToken(UUID userId) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        String token = stringRedisTemplate.opsForValue().get(key);
        log.debug("Retrieved refresh token for user: {}", userId);
        return token;
    }


    public void deleteRefreshToken(UUID userId) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        Boolean deleted = stringRedisTemplate.delete(key);
        log.info("Refresh token deleted for user: {}, success: {}", userId, deleted);
    }

    public boolean hasRefreshToken(UUID userId) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        return stringRedisTemplate.hasKey(key);
    }

    public void addToBlacklist(String accessToken, long expirationMinutes) {
        String key = BLACKLIST_PREFIX + accessToken;
        stringRedisTemplate.opsForValue().set(
                key,
                "blacklisted",
                expirationMinutes,
                TimeUnit.MINUTES
        );
        log.info("Access token added to blacklist");
    }

    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return stringRedisTemplate.hasKey(key);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
