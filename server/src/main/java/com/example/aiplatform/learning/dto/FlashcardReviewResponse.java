package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.FlashcardReviewResult;
import java.time.Instant;
import java.util.UUID;

public record FlashcardReviewResponse(
        UUID id,
        UUID flashcardId,
        FlashcardReviewResult result,
        Instant reviewedAt
) {
}
