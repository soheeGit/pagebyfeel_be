package org.pagebyfeel.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pagebyfeel.dto.response.AuthResponse;
import org.pagebyfeel.entity.user.User;
import org.pagebyfeel.exception.CustomException;
import org.pagebyfeel.exception.user.UserErrorCode;
import org.pagebyfeel.repository.UserRepository;
import org.pagebyfeel.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    public void saveRefreshToken(UUID userId, String refreshToken) {
        redisService.saveRefreshToken(userId, refreshToken, refreshTokenExpirationDays);
        log.info("Refresh token saved for user: {}", userId);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());

        String storedRefreshToken = redisService.getRefreshToken(userId);
        if (storedRefreshToken == null) {
            throw new CustomException(UserErrorCode.TOKEN_NOT_FOUND);
        }
        if (!storedRefreshToken.equals(refreshToken)) {
            throw new CustomException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getRole().name()
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        redisService.saveRefreshToken(userId, newRefreshToken, refreshTokenExpirationDays);

        log.info("Access token refreshed for user: {}", userId);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(UUID userId, String accessToken) {
        redisService.deleteRefreshToken(userId);

        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            redisService.addToBlacklist(accessToken, accessTokenExpirationMinutes);
        }

        log.info("User logged out: {}", userId);
    }

    public boolean hasValidRefreshToken(UUID userId) {
        return redisService.hasRefreshToken(userId);
    }
}
