package com.example.aiplatform.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateFlashcardsRequest(
        @Min(1) @Max(50) Integer count
) {
    public int effectiveCount() {
        return count == null ? 5 : count;
    }
}
