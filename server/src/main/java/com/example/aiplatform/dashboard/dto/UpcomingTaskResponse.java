package com.example.aiplatform.dashboard.dto;

import com.example.aiplatform.tasks.entity.TaskPriority;
import com.example.aiplatform.tasks.entity.TaskStatus;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingTaskResponse(
        UUID id,
        UUID learningGoalId,
        String learningGoalTitle,
        String title,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
) {
}
