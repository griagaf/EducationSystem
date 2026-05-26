package com.example.aiplatform.learning.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.file-storage.upload-dir=build/test-storage/materials")
class StudyMaterialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadTxtStoresMetadataAndExtractedText() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель с материалом");

        mockMvc.perform(multipart("/api/v1/goals/{goalId}/materials", goalId)
                        .file(txtFile("spring-notes.txt", "Spring Boot basics and dependency injection"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.learningGoalId").value(goalId))
                .andExpect(jsonPath("$.fileName").value("spring-notes.txt"))
                .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.fileSize").value(43))
                .andExpect(jsonPath("$.processingStatus").value("TEXT_EXTRACTED"))
                .andExpect(jsonPath("$.extractedText").value("Spring Boot basics and dependency injection"));
    }

    @Test
    void listMaterialsForGoal() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(token, "Цель со списком материалов");
        uploadMaterial(token, goalId, "first.txt", "First material text");
        uploadMaterial(token, goalId, "second.txt", "Second material text");

        mockMvc.perform(get("/api/v1/goals/{goalId}/materials", goalId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").isNotEmpty())
                .andExpect(jsonPath("$[0].processingStatus").value("TEXT_EXTRACTED"));
    }

    @Test
    void cannotAccessAnotherUsersMaterial() throws Exception {
        String ownerToken = registerAndGetToken(uniqueEmail());
        String anotherUserToken = registerAndGetToken(uniqueEmail());
        String goalId = createGoal(ownerToken, "Закрытый материал");
        String materialId = uploadMaterial(ownerToken, goalId, "private.txt", "Private material text");

        mockMvc.perform(get("/api/v1/materials/{materialId}", materialId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUserToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private String uploadMaterial(String token, String goalId, String fileName, String content) throws Exception {
        String response = mockMvc.perform(multipart("/api/v1/goals/{goalId}/materials", goalId)
                        .file(txtFile(fileName, content))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private MockMultipartFile txtFile(String fileName, String content) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
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
                                  "displayName": "Materials User"
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
        return "materials-" + UUID.randomUUID() + "@example.com";
    }
}
