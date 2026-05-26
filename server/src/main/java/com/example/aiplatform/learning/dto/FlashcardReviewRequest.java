package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.FlashcardReviewResult;
import jakarta.validation.constraints.NotNull;

public record FlashcardReviewRequest(
        @NotNull FlashcardReviewResult result
) {
}
