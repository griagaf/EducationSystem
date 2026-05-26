package com.example.aiplatform.dashboard.dto;

import com.example.aiplatform.learning.entity.LearningGoalStatus;
import com.example.aiplatform.learning.entity.LearningGoalType;
import java.time.Instant;
import java.util.UUID;

public record DashboardGoalResponse(
        UUID id,
        String title,
        LearningGoalType type,
        LearningGoalStatus status,
        int progressPercent,
        Instant createdAt
) {
}
