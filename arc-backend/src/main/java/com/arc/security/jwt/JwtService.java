package com.arc.security.jwt;

import com.arc.auth.enums.AppRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppRole role) {
        Instant issuedAt = Instant.now();
        Instant expiry = issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(role.name())
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Optional<AppRole> extractRole(String token) {
        try {
            Claims claims = parseClaims(token);
            String roleValue = claims.get(ROLE_CLAIM, String.class);

            if (roleValue == null) {
                roleValue = claims.getSubject();
            }

            return AppRole.fromString(roleValue);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public Instant extractIssuedAt(String token) {
        Claims claims = parseClaims(token);
        return claims.getIssuedAt().toInstant();
    }

    public Instant extractExpiry(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
