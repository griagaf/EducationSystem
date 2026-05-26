package com.example.aiplatform.ai.service;

import com.example.aiplatform.ai.dto.AiGenerateFlashcardsRequest;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsResponse;

public interface AiFlashcardClient {

    AiGenerateFlashcardsResponse generateFlashcards(AiGenerateFlashcardsRequest request);
}
