package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.LearningGoalType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LearningGoalCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull LearningGoalType type,
        LocalDate targetDate,
        @Min(1) @Max(52) Integer durationWeeks
) {
}
