package com.example.aiplatform.learning.dto;

import jakarta.validation.constraints.Pattern;

public record GenerateRoadmapRequest(
        @Pattern(regexp = "beginner|intermediate|advanced", message = "userLevel must be beginner, intermediate or advanced")
        String userLevel,
        Boolean includeMaterials
) {
    public String effectiveUserLevel() {
        return userLevel == null || userLevel.isBlank() ? "beginner" : userLevel;
    }
}
