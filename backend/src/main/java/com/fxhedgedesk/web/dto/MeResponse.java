package com.fxhedgedesk.web.dto;

import com.fxhedgedesk.domain.AppUser;

import java.util.UUID;

public record MeResponse(UUID id, String email, String displayName) {
    public static MeResponse from(AppUser user) {
        return new MeResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
