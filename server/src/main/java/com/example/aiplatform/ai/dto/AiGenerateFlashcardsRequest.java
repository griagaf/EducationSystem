package com.example.aiplatform.ai.dto;

import com.example.aiplatform.learning.entity.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AiGenerateFlashcardsRequest(
        @JsonProperty("goal_title") String goalTitle,
        @JsonProperty("topic_title") String topicTitle,
        @JsonProperty("topic_description") String topicDescription,
        @JsonProperty("difficulty_level") DifficultyLevel difficultyLevel,
        int count
) {
}
