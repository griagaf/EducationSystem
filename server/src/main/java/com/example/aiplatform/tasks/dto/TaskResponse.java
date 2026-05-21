package com.example.aiplatform.tasks.dto;

import com.example.aiplatform.tasks.entity.TaskPriority;
import com.example.aiplatform.tasks.entity.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID learningGoalId,
        String learningGoalTitle,
        UUID roadmapStepId,
        UUID topicId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
}
