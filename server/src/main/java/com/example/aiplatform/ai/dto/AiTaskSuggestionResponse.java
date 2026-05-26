package com.example.aiplatform.ai.dto;

public record AiTaskSuggestionResponse(
        String title,
        String description,
        String priority
) {
}
