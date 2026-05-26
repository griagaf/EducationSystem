package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.RoadmapStepStatus;
import java.util.UUID;

public record RoadmapStepResponse(
        UUID id,
        UUID topicId,
        String title,
        String description,
        int orderIndex,
        int estimatedDays,
        RoadmapStepStatus status
) {
}
