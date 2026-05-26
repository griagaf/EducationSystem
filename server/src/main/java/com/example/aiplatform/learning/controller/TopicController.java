package com.example.aiplatform.learning.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.learning.dto.TopicResponse;
import com.example.aiplatform.learning.service.TopicService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/goals/{goalId}/topics")
    public List<TopicResponse> getByGoal(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID goalId
    ) {
        return topicService.getByGoal(user.id(), goalId);
    }
}
