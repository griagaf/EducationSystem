package com.example.aiplatform.ai.dto;

import java.util.List;

public record AiGenerateFlashcardsResponse(
        List<AiFlashcardResponse> flashcards
) {
}
