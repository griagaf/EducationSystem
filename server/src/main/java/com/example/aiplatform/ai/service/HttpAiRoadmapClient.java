package com.example.aiplatform.ai.service;

import com.example.aiplatform.ai.dto.AiGenerateRoadmapRequest;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapResponse;
import com.example.aiplatform.common.exception.AiServiceException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class HttpAiRoadmapClient implements AiRoadmapClient {

    private final RestClient restClient;

    public HttpAiRoadmapClient(
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
    public AiGenerateRoadmapResponse generateRoadmap(AiGenerateRoadmapRequest request) {
        try {
            return restClient.post()
                    .uri("/api/ai/generate-roadmap")
                    .body(request)
                    .retrieve()
                    .body(AiGenerateRoadmapResponse.class);
        } catch (RestClientException exception) {
            throw new AiServiceException("AI-service is unavailable", exception);
        }
    }
}
