package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.DifficultyLevel;
import java.util.UUID;

public record TopicResponse(
        UUID id,
        UUID learningGoalId,
        String title,
        String description,
        int masteryScore,
        DifficultyLevel difficultyLevel
) {
}
