package com.example.aiplatform.learning.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LearningGoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGoalReturnsCreatedGoal() throws Exception {
        String token = registerAndGetToken(uniqueEmail());

        mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Изучить Spring Boot",
                                  "description": "Освоить backend-разработку на Spring Boot",
                                  "type": "TECHNOLOGY_LEARNING",
                                  "targetDate": "2026-08-15",
                                  "durationWeeks": 12
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Изучить Spring Boot"))
                .andExpect(jsonPath("$.type").value("TECHNOLOGY_LEARNING"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.targetDate").value("2026-08-15"))
                .andExpect(jsonPath("$.durationWeeks").value(12))
                .andExpect(jsonPath("$.progressPercent").value(0));
    }

    @Test
    void listReturnsOnlyCurrentUserGoals() throws Exception {
        String firstUserToken = registerAndGetToken(uniqueEmail());
        String secondUserToken = registerAndGetToken(uniqueEmail());

        createGoal(firstUserToken, "Первая цель пользователя");
        createGoal(secondUserToken, "Чужая цель");

        mockMvc.perform(get("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, bearer(firstUserToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Первая цель пользователя"));
    }

    @Test
    void cannotAccessAnotherUsersGoal() throws Exception {
        String ownerToken = registerAndGetToken(uniqueEmail());
        String anotherUserToken = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(ownerToken, "Закрытая цель");

        mockMvc.perform(get("/api/v1/goals/{goalId}", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUserToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private String createGoal(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/v1/goals")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Описание учебной цели",
                                  "type": "SELF_STUDY",
                                  "durationWeeks": 4
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
                                  "displayName": "Learning User"
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
        return "goal-" + UUID.randomUUID() + "@example.com";
    }
}
