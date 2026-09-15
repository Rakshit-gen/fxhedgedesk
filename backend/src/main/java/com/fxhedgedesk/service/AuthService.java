package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.exception.ConflictException;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthService {

    /** Every new desk starts with this much simulated USD to hedge with. */
    private static final BigDecimal STARTING_BALANCE = new BigDecimal("500000.00");

    private final AppUserRepository appUserRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, WalletService walletService,
                        PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(String email, String rawPassword, String displayName) {
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("An account with that email already exists");
        }
        AppUser user = new AppUser(email, passwordEncoder.encode(rawPassword), displayName);
        appUserRepository.save(user);
        walletService.openWallet(user, STARTING_BALANCE);
        return jwtService.issue(user.getId(), user.getEmail());
    }

    public String login(String email, String rawPassword) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return jwtService.issue(user.getId(), user.getEmail());
    }

    public AppUser requireById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
