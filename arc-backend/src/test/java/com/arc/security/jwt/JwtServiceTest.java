package com.arc.security.jwt;

import com.arc.auth.enums.AppRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "arc-manufacturing-jwt-secret-key-change-in-production-min-256-bits",
                3_600_000L
        );
    }

    @Test
    void generateToken_containsRoleClaim() {
        String token = jwtService.generateToken(AppRole.MANAGER);

        Optional<AppRole> role = jwtService.extractRole(token);

        assertThat(role).contains(AppRole.MANAGER);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractRole_withInvalidToken_returnsEmpty() {
        Optional<AppRole> role = jwtService.extractRole("invalid.token.value");

        assertThat(role).isEmpty();
        assertThat(jwtService.isTokenValid("invalid.token.value")).isFalse();
    }

    @Test
    void token_containsIssuedAndExpiryTimes() {
        String token = jwtService.generateToken(AppRole.OPERATOR);

        Instant issuedAt = jwtService.extractIssuedAt(token);
        Instant expiry = jwtService.extractExpiry(token);

        assertThat(issuedAt).isBefore(expiry);
        assertThat(expiry).isAfter(Instant.now());
    }
}
