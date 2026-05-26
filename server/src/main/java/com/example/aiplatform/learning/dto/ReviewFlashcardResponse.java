package com.example.aiplatform.learning.dto;

public record ReviewFlashcardResponse(
        FlashcardReviewResponse review,
        int topicMasteryScore
) {
}
