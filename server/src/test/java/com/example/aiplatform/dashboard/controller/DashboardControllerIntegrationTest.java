package com.example.aiplatform.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRoadmapClient aiRoadmapClient;

    @Test
    void dashboardReturnsOnlyCurrentUserData() throws Exception {
        String firstUserToken = registerAndGetToken(uniqueEmail());
        String secondUserToken = registerAndGetToken(uniqueEmail());
        String firstGoalId = createGoal(firstUserToken, "Своя цель");
        String secondGoalId = createGoal(secondUserToken, "Чужая цель");
        createTask(firstUserToken, firstGoalId, "Своя задача");
        createTask(secondUserToken, secondGoalId, "Чужая задача");

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(firstUserToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGoals").value(1))
                .andExpect(jsonPath("$.activeGoals").value(1))
                .andExpect(jsonPath("$.totalTasks").value(1))
                .andExpect(jsonPath("$.recentGoals.length()").value(1))
                .andExpect(jsonPath("$.recentGoals[0].title").value("Своя цель"))
                .andExpect(jsonPath("$.upcomingTasks.length()").value(1))
                .andExpect(jsonPath("$.upcomingTasks[0].title").value("Своя задача"));
    }

    @Test
    void completionPercentCalculatedCorrectly() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с процентом");
        String doneTaskId = createTask(token, goalId, "Готовая задача");
        createTask(token, goalId, "Открытая задача");

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", doneTaskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.completedTasks").value(1))
                .andExpect(jsonPath("$.taskCompletionPercent").value(50));
    }

    @Test
    void weakTopicsCalculatedCorrectly() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с темой");
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

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageMasteryScore").value(0))
                .andExpect(jsonPath("$.weakTopics.length()").value(1))
                .andExpect(jsonPath("$.weakTopics[0].title").value("Dashboard Topic"))
                .andExpect(jsonPath("$.weakTopics[0].masteryScore").value(0));
    }

    private String createTask(String token, String goalId, String title) throws Exception {
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "learningGoalId": "%s",
                                  "title": "%s",
                                  "description": "Описание задачи",
                                  "priority": "MEDIUM",
                                  "dueDate": "2026-06-10"
                                }
                                """.formatted(goalId, title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
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
                "Dashboard roadmap",
                "Roadmap for dashboard weak topics",
                List.of(new AiRoadmapStepResponse(
                        "Dashboard Step",
                        "Описание этапа",
                        1,
                        5,
                        List.of("Dashboard Topic"),
                        List.of(new AiTaskSuggestionResponse("Dashboard task", "Описание", "MEDIUM"))
                ))
        );
    }

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123",
                                  "displayName": "Dashboard User"
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
        return "dashboard-" + UUID.randomUUID() + "@example.com";
    }
}
