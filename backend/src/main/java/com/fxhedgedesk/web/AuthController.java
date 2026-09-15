package com.fxhedgedesk.web;

import com.fxhedgedesk.service.AuthService;
import com.fxhedgedesk.web.dto.AuthDtos.AuthResponse;
import com.fxhedgedesk.web.dto.AuthDtos.LoginRequest;
import com.fxhedgedesk.web.dto.AuthDtos.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.email(), request.password(), request.displayName());
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new AuthResponse(token);
    }
}
