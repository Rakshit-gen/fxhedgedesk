package com.fxhedgedesk.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SecurityException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private final JwtService jwtService = new JwtService(SECRET, 60);

    @Test
    void aTokenParsesBackToTheUserIdItWasIssuedFor() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.issue(userId, "desk@example.com");

        assertThat(jwtService.parseUserId(token)).isEqualTo(userId);
    }

    @Test
    void aTokenSignedWithADifferentSecretIsRejected() {
        JwtService other = new JwtService("different-secret-different-secret-different-y", 60);
        String token = other.issue(UUID.randomUUID(), "desk@example.com");

        assertThatThrownBy(() -> jwtService.parseUserId(token)).isInstanceOf(SecurityException.class);
    }

    @Test
    void anAlreadyExpiredTokenIsRejected() throws InterruptedException {
        JwtService instantlyExpiring = new JwtService(SECRET, 0);
        String token = instantlyExpiring.issue(UUID.randomUUID(), "desk@example.com");
        Thread.sleep(5);

        assertThatThrownBy(() -> instantlyExpiring.parseUserId(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
