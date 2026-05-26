package com.example.aiplatform.learning.dto;

import com.example.aiplatform.learning.entity.StudyMaterialProcessingStatus;
import java.time.Instant;
import java.util.UUID;

public record StudyMaterialResponse(
        UUID id,
        UUID learningGoalId,
        String fileName,
        String contentType,
        long fileSize,
        StudyMaterialProcessingStatus processingStatus,
        String extractedText,
        Instant createdAt
) {
}
