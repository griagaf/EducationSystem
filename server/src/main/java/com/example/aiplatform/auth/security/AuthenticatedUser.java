package com.example.aiplatform.auth.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String email) {
}
