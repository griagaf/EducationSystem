package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.DifficultyLevel;
import java.time.Instant;
import java.util.UUID;

public record FlashcardResponse(
        UUID id,
        UUID learningGoalId,
        UUID topicId,
        String question,
        String answer,
        DifficultyLevel difficulty,
        Instant createdAt,
        Instant updatedAt
) {
}
