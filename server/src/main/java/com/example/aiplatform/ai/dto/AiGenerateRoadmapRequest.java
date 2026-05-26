package com.example.aiplatform.ai.dto;

import com.example.aiplatform.learning.entity.LearningGoalType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AiGenerateRoadmapRequest(
        @JsonProperty("goal_title") String goalTitle,
        @JsonProperty("goal_description") String goalDescription,
        @JsonProperty("goal_type") LearningGoalType goalType,
        @JsonProperty("target_date") String targetDate,
        @JsonProperty("estimated_duration_weeks") Integer estimatedDurationWeeks,
        @JsonProperty("user_level") String userLevel
) {
}
