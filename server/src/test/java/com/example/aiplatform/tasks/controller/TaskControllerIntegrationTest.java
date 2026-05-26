package com.example.aiplatform.tasks.controller;

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
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRoadmapClient aiRoadmapClient;

    @Test
    void listReturnsOnlyCurrentUserTasks() throws Exception {
        String firstUserToken = registerAndGetToken(uniqueEmail());
        String secondUserToken = registerAndGetToken(uniqueEmail());
        String firstGoalId = createGoal(firstUserToken, "Первая цель");
        String secondGoalId = createGoal(secondUserToken, "Чужая цель");
        createTask(firstUserToken, firstGoalId, "Своя задача");
        createTask(secondUserToken, secondGoalId, "Чужая задача");

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(firstUserToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Своя задача"));
    }

    @Test
    void createTaskReturnsCreatedTask() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель для задачи");

        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "learningGoalId": "%s",
                                  "title": "Прочитать документацию",
                                  "description": "Разобрать базовые разделы",
                                  "priority": "HIGH",
                                  "dueDate": "2026-06-10"
                                }
                                """.formatted(goalId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.learningGoalId").value(goalId))
                .andExpect(jsonPath("$.learningGoalTitle").value("Цель для задачи"))
                .andExpect(jsonPath("$.title").value("Прочитать документацию"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void changeStatusUpdatesTaskAndGoalProgress() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с прогрессом");
        String taskId = createTask(token, goalId, "Завершить задачу");

        mockMvc.perform(patch("/api/v1/tasks/{taskId}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(get("/api/v1/goals/{goalId}", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercent").value(100));
    }

    @Test
    void cannotAccessAnotherUsersTask() throws Exception {
        String ownerToken = registerAndGetToken(uniqueEmail());
        String anotherUserToken = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(ownerToken, "Закрытая цель");
        String taskId = createTask(ownerToken, goalId, "Закрытая задача");

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUserToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void roadmapGenerationCreatesTasks() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Roadmap goal");
        when(aiRoadmapClient.generateRoadmap(any(AiGenerateRoadmapRequest.class))).thenReturn(aiRoadmapResponse());

        mockMvc.perform(post("/api/v1/goals/{goalId}/generate-roadmap", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userLevel": "beginner"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdTasks.length()").value(2))
                .andExpect(jsonPath("$.createdTasks[0].status").value("TODO"));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("learningGoalId", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].learningGoalId").value(goalId));
    }

    private AiGenerateRoadmapResponse aiRoadmapResponse() {
        return new AiGenerateRoadmapResponse(
                "Roadmap с задачами",
                "План с задачами",
                List.of(
                        new AiRoadmapStepResponse(
                                "Первый этап",
                                "Описание первого этапа",
                                1,
                                5,
                                List.of("Topic 1"),
                                List.of(new AiTaskSuggestionResponse("Задача этапа 1", "Описание", "MEDIUM"))
                        ),
                        new AiRoadmapStepResponse(
                                "Второй этап",
                                "Описание второго этапа",
                                2,
                                6,
                                List.of("Topic 2"),
                                List.of(new AiTaskSuggestionResponse("Задача этапа 2", "Описание", "HIGH"))
                        )
                )
        );
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
                                  "priority": "MEDIUM"
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

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123",
                                  "displayName": "Tasks User"
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
        return "tasks-" + UUID.randomUUID() + "@example.com";
    }
}
