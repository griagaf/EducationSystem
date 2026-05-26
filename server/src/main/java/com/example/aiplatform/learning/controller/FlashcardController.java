package com.example.aiplatform.learning.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.learning.dto.FlashcardResponse;
import com.example.aiplatform.learning.dto.FlashcardReviewRequest;
import com.example.aiplatform.learning.dto.GenerateFlashcardsRequest;
import com.example.aiplatform.learning.dto.GenerateFlashcardsResponse;
import com.example.aiplatform.learning.dto.ReviewFlashcardResponse;
import com.example.aiplatform.learning.service.FlashcardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping("/topics/{topicId}/generate-flashcards")
    public ResponseEntity<GenerateFlashcardsResponse> generate(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID topicId,
            @Valid @RequestBody(required = false) GenerateFlashcardsRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(flashcardService.generate(user.id(), topicId, request));
    }

    @GetMapping("/topics/{topicId}/flashcards")
    public List<FlashcardResponse> getByTopic(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID topicId
    ) {
        return flashcardService.getByTopic(user.id(), topicId);
    }

    @GetMapping("/flashcards/{flashcardId}")
    public FlashcardResponse getById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID flashcardId
    ) {
        return flashcardService.getById(user.id(), flashcardId);
    }

    @PostMapping("/flashcards/{flashcardId}/review")
    public ReviewFlashcardResponse review(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID flashcardId,
            @Valid @RequestBody FlashcardReviewRequest request
    ) {
        return flashcardService.review(user.id(), flashcardId, request);
    }
}
