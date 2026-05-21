package com.example.aiplatform.auth.service;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String jwtSecret;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${app.jwt-secret}") String jwtSecret,
            @Value("${app.jwt-access-token-ttl}") Duration accessTokenTtl
    ) {
        this.jwtSecret = jwtSecret;
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateToken(UUID userId, String email) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthenticatedUser(UUID.fromString(claims.getSubject()), claims.get("email", String.class));
    }

    private SecretKey signingKey() {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
