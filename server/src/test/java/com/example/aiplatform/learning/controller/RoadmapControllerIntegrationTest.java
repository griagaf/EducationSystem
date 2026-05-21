package com.example.aiplatform.learning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiplatform.ai.dto.AiGenerateRoadmapRequest;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapResponse;
import com.example.aiplatform.ai.dto.AiRoadmapStepResponse;
import com.example.aiplatform.ai.dto.AiTaskSuggestionResponse;
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
class RoadmapControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRoadmapClient aiRoadmapClient;

    @Test
    void generateRoadmapSavesRoadmapTopicsAndSteps() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Изучить Spring Boot");
        when(aiRoadmapClient.generateRoadmap(any(AiGenerateRoadmapRequest.class))).thenReturn(aiRoadmapResponse());

        mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner",
                                  "includeMaterials": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roadmap.id").isNotEmpty())
                .andExpect(jsonPath("$.roadmap.learningGoalId").value(goalId))
                .andExpect(jsonPath("$.roadmap.title").value("Spring Boot roadmap"))
                .andExpect(jsonPath("$.roadmap.steps.length()").value(2))
                .andExpect(jsonPath("$.roadmap.steps[0].orderIndex").value(1))
                .andExpect(jsonPath("$.roadmap.steps[0].estimatedDays").value(7))
                .andExpect(jsonPath("$.roadmap.steps[0].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.topics.length()").value(3))
                .andExpect(jsonPath("$.createdTasks.length()").value(0));
    }

    @Test
    void cannotGenerateRoadmapForAnotherUsersGoal() throws Exception {
        String ownerToken = registerAndGetToken(uniqueEmail());
        String anotherUserToken = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(ownerToken, "Закрытая цель");

        mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verifyNoInteractions(aiRoadmapClient);
    }

    @Test
    void cannotGenerateDuplicateRoadmap() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с roadmap");
        when(aiRoadmapClient.generateRoadmap(any(AiGenerateRoadmapRequest.class))).thenReturn(aiRoadmapResponse());

        mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROADMAP_ALREADY_EXISTS"));
    }

    @Test
    void getRoadmapReturnsSavedRoadmap() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Roadmap details goal");
        when(aiRoadmapClient.generateRoadmap(any(AiGenerateRoadmapRequest.class))).thenReturn(aiRoadmapResponse());

        String generated = mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "intermediate"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String roadmapId = objectMapper.readTree(generated).get("roadmap").get("id").asText();

        mockMvc.perform(get("/api/v1/goals/{goalId}/roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roadmapId))
                .andExpect(jsonPath("$.learningGoalId").value(goalId))
                .andExpect(jsonPath("$.steps.length()").value(2));

        mockMvc.perform(get("/api/v1/roadmaps/{roadmapId}", roadmapId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roadmapId))
                .andExpect(jsonPath("$.steps[1].orderIndex").value(2));
    }

    private AiGenerateRoadmapResponse aiRoadmapResponse() {
        return new AiGenerateRoadmapResponse(
                "Spring Boot roadmap",
                "План изучения Spring Boot",
                List.of(
                        new AiRoadmapStepResponse(
                                "Основы Spring",
                                "Изучить IoC, REST controllers и структуру проекта",
                                1,
                                7,
                                List.of("Spring IoC", "REST API"),
                                List.of(new AiTaskSuggestionResponse("Повторить IoC", "Разобрать DI", "MEDIUM"))
                        ),
                        new AiRoadmapStepResponse(
                                "Практический backend",
                                "Собрать простой REST backend",
                                2,
                                10,
                                List.of("Spring Data JPA"),
                                List.of(new AiTaskSuggestionResponse("Сделать CRUD", "Создать API", "HIGH"))
                        )
                )
        );
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
                                  "targetDate": "2026-08-15",
                                  "durationWeeks": 12
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123",
                                  "displayName": "Roadmap User"
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
        return "roadmap-" + UUID.randomUUID() + "@example.com";
    }
}
