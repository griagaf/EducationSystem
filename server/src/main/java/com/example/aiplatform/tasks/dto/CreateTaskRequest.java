package com.example.aiplatform.tasks.dto;

import com.example.aiplatform.tasks.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotNull UUID learningGoalId,
        UUID roadmapStepId,
        UUID topicId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 5000) String description,
        @NotNull TaskPriority priority,
        LocalDate dueDate
) {
}
