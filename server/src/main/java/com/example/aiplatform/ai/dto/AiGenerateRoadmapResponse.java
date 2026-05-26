package com.example.aiplatform.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiGenerateRoadmapResponse(
        @JsonProperty("roadmap_title") String roadmapTitle,
        @JsonProperty("roadmap_description") String roadmapDescription,
        List<AiRoadmapStepResponse> steps
) {
}
