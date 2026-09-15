package com.fxhedgedesk.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import com.fxhedgedesk.service.AuthService;
import com.fxhedgedesk.web.dto.AuthDtos.LoginRequest;
import com.fxhedgedesk.web.dto.AuthDtos.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void registeringWithAMalformedEmailIsRejectedBeforeReachingTheService() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registeringWithATooShortPasswordIsRejected() throws Exception {
        RegisterRequest request = new RegisterRequest("new@example.com", "short", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registeringWithValidDataReturnsAToken() throws Exception {
        when(authService.register("new@example.com", "password123", "New User")).thenReturn("a.jwt.token");
        RegisterRequest request = new RegisterRequest("new@example.com", "password123", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.jwt.token"));
    }

    @Test
    void loggingInWithWrongCredentialsMapsTo401() throws Exception {
        when(authService.login(any(), any())).thenThrow(new BadCredentialsException("nope"));
        LoginRequest request = new LoginRequest("desk@example.com", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
