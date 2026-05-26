package com.example.aiplatform.learning.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.learning.dto.GenerateRoadmapRequest;
import com.example.aiplatform.learning.dto.GenerateRoadmapResponse;
import com.example.aiplatform.learning.dto.RoadmapResponse;
import com.example.aiplatform.learning.service.RoadmapGenerationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RoadmapController {

    private final RoadmapGenerationService roadmapGenerationService;

    public RoadmapController(RoadmapGenerationService roadmapGenerationService) {
        this.roadmapGenerationService = roadmapGenerationService;
    }

    @PostMapping("/goals/{goalId}/generate-roadmap")
    public GenerateRoadmapResponse generateRoadmap(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId,
            @Valid @RequestBody(required = false) GenerateRoadmapRequest request
    ) {
        return roadmapGenerationService.generate(user.id(), goalId, request);
    }

    @GetMapping("/goals/{goalId}/roadmap")
    public RoadmapResponse getRoadmapByGoal(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId
    ) {
        return roadmapGenerationService.getByGoalId(user.id(), goalId);
    }

    @GetMapping("/roadmaps/{roadmapId}")
    public RoadmapResponse getRoadmapById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID roadmapId
    ) {
        return roadmapGenerationService.getById(user.id(), roadmapId);
    }
}
