package com.example.aiplatform.learning.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.learning.dto.StudyMaterialResponse;
import com.example.aiplatform.learning.service.StudyMaterialService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    public StudyMaterialController(StudyMaterialService studyMaterialService) {
        this.studyMaterialService = studyMaterialService;
    }

    @PostMapping(value = "/goals/{goalId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudyMaterialResponse> upload(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studyMaterialService.upload(user.id(), goalId, file));
    }

    @GetMapping("/goals/{goalId}/materials")
    public List<StudyMaterialResponse> getByGoal(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId
    ) {
        return studyMaterialService.getByGoal(user.id(), goalId);
    }

    @GetMapping("/materials/{materialId}")
    public StudyMaterialResponse getById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID materialId
    ) {
        return studyMaterialService.getById(user.id(), materialId);
    }
}
