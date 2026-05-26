package com.example.aiplatform.learning.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.learning.dto.DeleteLearningGoalResponse;
import com.example.aiplatform.learning.dto.LearningGoalCreateRequest;
import com.example.aiplatform.learning.dto.LearningGoalResponse;
import com.example.aiplatform.learning.dto.LearningGoalUpdateRequest;
import com.example.aiplatform.learning.service.LearningGoalService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/goals")
public class LearningGoalController {

    private final LearningGoalService learningGoalService;

    public LearningGoalController(LearningGoalService learningGoalService) {
        this.learningGoalService = learningGoalService;
    }

    @GetMapping
    public List<LearningGoalResponse> getList(@AuthenticationPrincipal AuthenticatedUser user) {
        return learningGoalService.getList(user.id());
    }

    @PostMapping
    public ResponseEntity<LearningGoalResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody LearningGoalCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(learningGoalService.create(user.id(), request));
    }

    @GetMapping("/{goalId}")
    public LearningGoalResponse getById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId
    ) {
        return learningGoalService.getById(user.id(), goalId);
    }

    @PutMapping("/{goalId}")
    public LearningGoalResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId,
            @Valid @RequestBody LearningGoalUpdateRequest request
    ) {
        return learningGoalService.update(user.id(), goalId, request);
    }

    @DeleteMapping("/{goalId}")
    public DeleteLearningGoalResponse delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId
    ) {
        return learningGoalService.archive(user.id(), goalId);
    }
}
