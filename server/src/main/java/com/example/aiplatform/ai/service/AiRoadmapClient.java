package com.example.aiplatform.ai.service;

import com.example.aiplatform.ai.dto.AiGenerateRoadmapRequest;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapResponse;

public interface AiRoadmapClient {

    AiGenerateRoadmapResponse generateRoadmap(AiGenerateRoadmapRequest request);
}
