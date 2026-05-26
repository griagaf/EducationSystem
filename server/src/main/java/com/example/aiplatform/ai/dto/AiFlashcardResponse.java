package com.example.aiplatform.ai.dto;

public record AiFlashcardResponse(
        String question,
        String answer,
        String difficulty
) {
}
