package com.example.aiplatform.learning.dto;

import java.util.List;

public record GenerateRoadmapResponse(
        RoadmapResponse roadmap,
        List<TopicResponse> topics,
        List<CreatedTaskResponse> createdTasks
) {
}
