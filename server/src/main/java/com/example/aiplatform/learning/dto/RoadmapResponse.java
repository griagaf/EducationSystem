package com.example.aiplatform.learning.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        UUID learningGoalId,
        String title,
        String description,
        List<RoadmapStepResponse> steps,
        Instant createdAt,
        Instant updatedAt
) {
}
