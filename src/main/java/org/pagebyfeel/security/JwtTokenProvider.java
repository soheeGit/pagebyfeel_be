package org.pagebyfeel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.pagebyfeel.entity.user.User;
import org.pagebyfeel.exception.auth.AuthErrorCode;
import org.pagebyfeel.exception.common.BusinessException;
import org.pagebyfeel.repository.UserRepository;
import org.pagebyfeel.security.oauth.CustomOAuth2User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMillis;
    private final long refreshTokenValidityInMillis;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @org.springframework.beans.factory.annotation.Value("${jwt.secret-key}") String secretKey,
            @org.springframework.beans.factory.annotation.Value("${jwt.access-token-expiration-minutes}") long accessTokenMinutes,
            @org.springframework.beans.factory.annotation.Value("${jwt.refresh-token-expiration-days}") long refreshTokenDays,
            UserRepository userRepository
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret key must be at least 256 bits (32 bytes)"
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMillis = accessTokenMinutes * 60 * 1000;
        this.refreshTokenValidityInMillis = refreshTokenDays * 24 * 60 * 60 * 1000;
        this.userRepository = userRepository;
    }

    public String generateAccessToken(UUID userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidityInMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidityInMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);
        UUID userId = UUID.fromString(claims.getSubject());
        String role = claims.get("role", String.class);

        if (role == null || role.isEmpty()) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_TOKEN));

        CustomOAuth2User principal = new CustomOAuth2User(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProvider(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role)),
                new HashMap<>()
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}