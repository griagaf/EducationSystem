package com.example.aiplatform.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiRoadmapStepResponse(
        String title,
        String description,
        @JsonProperty("order_index") Integer orderIndex,
        @JsonProperty("estimated_days") Integer estimatedDays,
        List<String> topics,
        List<AiTaskSuggestionResponse> tasks
) {
}
