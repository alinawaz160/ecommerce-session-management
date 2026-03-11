package com.ecommerce.cart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpirySeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpirySeconds * 1000L;
    }

    public String generateAccessToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiry))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Claims validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(validateAccessToken(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return (String) validateAccessToken(token).get("email");
    }

    public boolean isTokenValid(String token) {
        try {
            validateAccessToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
