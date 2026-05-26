package com.example.aiplatform.learning.dto;

import java.util.UUID;

public record CreatedTaskResponse(
        UUID id,
        String title,
        String status,
        String priority
) {
}
