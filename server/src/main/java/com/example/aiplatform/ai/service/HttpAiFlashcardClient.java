package com.example.aiplatform.ai.service;

import com.example.aiplatform.ai.dto.AiGenerateFlashcardsRequest;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsResponse;
import com.example.aiplatform.common.exception.AiServiceException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class HttpAiFlashcardClient implements AiFlashcardClient {

    private final RestClient restClient;

    public HttpAiFlashcardClient(
            @Value("${app.ai-service-url}") String aiServiceUrl,
            @Value("${app.ai-timeout:PT10S}") Duration aiTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(aiTimeout);
        requestFactory.setReadTimeout(aiTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(aiServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public AiGenerateFlashcardsResponse generateFlashcards(AiGenerateFlashcardsRequest request) {
        try {
            return restClient.post()
                    .uri("/api/ai/generate-flashcards")
                    .body(request)
                    .retrieve()
                    .body(AiGenerateFlashcardsResponse.class);
        } catch (RestClientException exception) {
            throw new AiServiceException("AI-service is unavailable", exception);
        }
    }
}
