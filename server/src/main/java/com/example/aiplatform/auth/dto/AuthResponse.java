package com.example.aiplatform.auth.dto;

public record AuthResponse(
        String accessToken,
        CurrentUserResponse user
) {
}
