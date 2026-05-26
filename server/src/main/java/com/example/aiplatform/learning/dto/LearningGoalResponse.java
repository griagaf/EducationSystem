package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.LearningGoalStatus;
import com.example.aiplatform.learning.entity.LearningGoalType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LearningGoalResponse(
        UUID id,
        String title,
        String description,
        LearningGoalType type,
        LearningGoalStatus status,
        LocalDate targetDate,
        Integer durationWeeks,
        String estimatedDuration,
        int progressPercent,
        Instant createdAt,
        Instant updatedAt
) {
}
