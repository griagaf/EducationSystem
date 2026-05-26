package com.example.aiplatform.learning.dto;

import java.util.List;

public record GenerateFlashcardsResponse(
        List<FlashcardResponse> createdFlashcards
) {
}
