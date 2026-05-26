package com.example.aiplatform.dashboard.dto;

import com.example.aiplatform.learning.entity.DifficultyLevel;
import java.util.UUID;

public record DashboardTopicResponse(
        UUID id,
        UUID learningGoalId,
        String title,
        int masteryScore,
        DifficultyLevel difficultyLevel
) {
}
