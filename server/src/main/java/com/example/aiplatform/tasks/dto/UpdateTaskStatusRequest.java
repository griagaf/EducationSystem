package com.example.aiplatform.tasks.dto;

import com.example.aiplatform.tasks.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull TaskStatus status
) {
}
