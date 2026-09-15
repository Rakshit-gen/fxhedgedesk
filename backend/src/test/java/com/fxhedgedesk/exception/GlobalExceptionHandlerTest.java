package com.fxhedgedesk.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundMapsTo404() {
        ResponseEntity<ApiError> response = handler.handleNotFound(new NotFoundException("Exposure not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Exposure not found");
    }

    @Test
    void conflictMapsTo409() {
        ResponseEntity<ApiError> response = handler.handleConflict(new ConflictException("Email already in use"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void illegalArgumentMapsTo400() {
        ResponseEntity<ApiError> response = handler.handleBadRequest(new IllegalArgumentException("Bad notional"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Bad notional");
    }

    @Test
    void illegalStateMapsTo400() {
        ResponseEntity<ApiError> response = handler.handleBadRequest(new IllegalStateException("Already settled"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void badCredentialsMapsTo401WithoutLeakingWhichFieldWasWrong() {
        ResponseEntity<ApiError> response = handler.handleBadCredentials(new BadCredentialsException("nope"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
    }
}
