package com.example.aiplatform.learning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiplatform.ai.dto.AiFlashcardResponse;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsRequest;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsResponse;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapRequest;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapResponse;
import com.example.aiplatform.ai.dto.AiRoadmapStepResponse;
import com.example.aiplatform.ai.dto.AiTaskSuggestionResponse;
import com.example.aiplatform.ai.service.AiFlashcardClient;
import com.example.aiplatform.ai.service.AiRoadmapClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlashcardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRoadmapClient aiRoadmapClient;

    @MockBean
    private AiFlashcardClient aiFlashcardClient;

    @Test
    void generateFlashcardsWithMockedAiClient() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String topicId = createTopicThroughRoadmap(token);
        when(aiFlashcardClient.generateFlashcards(any(AiGenerateFlashcardsRequest.class))).thenReturn(aiFlashcardsResponse());

        mockMvc.perform(post("/api/v1/topics/{topicId}/generate-flashcards", topicId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "count": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdFlashcards.length()").value(2))
                .andExpect(jsonPath("$.createdFlashcards[0].topicId").value(topicId))
                .andExpect(jsonPath("$.createdFlashcards[0].question").value("Что такое DI?"))
                .andExpect(jsonPath("$.createdFlashcards[0].difficulty").value("MEDIUM"));
    }

    @Test
    void listFlashcardsByTopic() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String topicId = createTopicThroughRoadmap(token);
        when(aiFlashcardClient.generateFlashcards(any(AiGenerateFlashcardsRequest.class))).thenReturn(aiFlashcardsResponse());
        generateFlashcards(token, topicId);

        mockMvc.perform(get("/api/v1/topics/{topicId}/flashcards", topicId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].topicId").value(topicId))
                .andExpect(jsonPath("$[1].answer").value("DI помогает уменьшить связность компонентов."));
    }

    @Test
    void reviewKnowUpdatesMasteryScore() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с карточками");
        String topicId = createTopicThroughRoadmap(token, goalId);
        when(aiFlashcardClient.generateFlashcards(any(AiGenerateFlashcardsRequest.class))).thenReturn(aiFlashcardsResponse());
        String flashcardId = generateFlashcards(token, topicId).get("createdFlashcards").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/flashcards/{flashcardId}/review", flashcardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "result": "KNOW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review.id").isNotEmpty())
                .andExpect(jsonPath("$.review.flashcardId").value(flashcardId))
                .andExpect(jsonPath("$.review.result").value("KNOW"))
                .andExpect(jsonPath("$.topicMasteryScore").value(15));
    }

    @Test
    void cannotReviewAnotherUsersFlashcard() throws Exception {
        String ownerToken = registerAndGetToken(uniqueEmail());
        String anotherUserToken = registerAndGetToken(uniqueEmail());
        String topicId = createTopicThroughRoadmap(ownerToken);
        when(aiFlashcardClient.generateFlashcards(any(AiGenerateFlashcardsRequest.class))).thenReturn(aiFlashcardsResponse());
        String flashcardId = generateFlashcards(ownerToken, topicId).get("createdFlashcards").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/flashcards/{flashcardId}/review", flashcardId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "result": "KNOW"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private JsonNode generateFlashcards(String token, String topicId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/topics/{topicId}/generate-flashcards", topicId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "count": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String createTopicThroughRoadmap(String token) throws Exception {
        String goalId = createGoal(token, "Цель с topic");
        return createTopicThroughRoadmap(token, goalId);
    }

    private String createTopicThroughRoadmap(String token, String goalId) throws Exception {
        when(aiRoadmapClient.generateRoadmap(any(AiGenerateRoadmapRequest.class))).thenReturn(aiRoadmapResponse());
        String response = mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("topics").get(0).get("id").asText();
    }

    private String createGoal(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Описание учебной цели",
                                  "type": "TECHNOLOGY_LEARNING",
                                  "durationWeeks": 8
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private AiGenerateRoadmapResponse aiRoadmapResponse() {
        return new AiGenerateRoadmapResponse(
                "Roadmap для flashcards",
                "План с темой для карточек",
                List.of(
                        new AiRoadmapStepResponse(
                                "Dependency Injection",
                                "Изучить передачу зависимостей",
                                1,
                                5,
                                List.of("Dependency Injection"),
                                List.of(new AiTaskSuggestionResponse("Разобрать DI", "Описание", "MEDIUM"))
                        )
                )
        );
    }

    private AiGenerateFlashcardsResponse aiFlashcardsResponse() {
        return new AiGenerateFlashcardsResponse(List.of(
                new AiFlashcardResponse("Что такое DI?", "Dependency Injection передает зависимости извне.", "MEDIUM"),
                new AiFlashcardResponse("Зачем нужен DI?", "DI помогает уменьшить связность компонентов.", "MEDIUM")
        ));
    }

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123",
                                  "displayName": "Flashcard User"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail() {
        return "flashcards-" + UUID.randomUUID() + "@example.com";
    }
}
