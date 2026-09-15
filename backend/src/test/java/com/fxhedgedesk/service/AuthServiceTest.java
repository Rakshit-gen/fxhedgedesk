package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.exception.ConflictException;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AppUserRepository appUserRepository;
    private WalletService walletService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        walletService = mock(WalletService.class);
        JwtService jwtService = new JwtService("test-secret-test-secret-test-secret-test-secret", 60);
        authService = new AuthService(appUserRepository, walletService, passwordEncoder, jwtService);
    }

    @Test
    void registeringWithAnAlreadyUsedEmailIsRejected() {
        when(appUserRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("taken@example.com", "password123", "Someone"))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(walletService);
    }

    @Test
    void registeringOpensAWalletForTheSavedUserNotTheTransientOne() {
        AppUser saved = new AppUser("new@example.com", "hash", "New User");
        when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(appUserRepository.save(any())).thenReturn(saved);

        String token = authService.register("new@example.com", "password123", "New User");

        assertThat(token).isNotBlank();
        verify(walletService).openWallet(saved, new BigDecimal("500000.00"));
    }

    @Test
    void loggingInWithAWrongPasswordIsRejected() {
        AppUser user = new AppUser("desk@example.com", passwordEncoder.encode("correct-password"), "Desk");
        when(appUserRepository.findByEmail("desk@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("desk@example.com", "wrong-password"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loggingInWithAnUnknownEmailIsRejected() {
        when(appUserRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody@example.com", "anything"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loggingInWithCorrectCredentialsIssuesAToken() {
        AppUser user = new AppUser("desk@example.com", passwordEncoder.encode("correct-password"), "Desk");
        when(appUserRepository.findByEmail("desk@example.com")).thenReturn(Optional.of(user));

        String token = authService.login("desk@example.com", "correct-password");

        assertThat(token).isNotBlank();
    }
}
