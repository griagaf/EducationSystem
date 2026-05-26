package com.example.aiplatform.learning.service;

import com.example.aiplatform.ai.dto.AiFlashcardResponse;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsRequest;
import com.example.aiplatform.ai.dto.AiGenerateFlashcardsResponse;
import com.example.aiplatform.ai.service.AiFlashcardClient;
import com.example.aiplatform.common.exception.InvalidAiResponseException;
import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.learning.dto.FlashcardResponse;
import com.example.aiplatform.learning.dto.FlashcardReviewRequest;
import com.example.aiplatform.learning.dto.FlashcardReviewResponse;
import com.example.aiplatform.learning.dto.GenerateFlashcardsRequest;
import com.example.aiplatform.learning.dto.GenerateFlashcardsResponse;
import com.example.aiplatform.learning.dto.ReviewFlashcardResponse;
import com.example.aiplatform.learning.entity.DifficultyLevel;
import com.example.aiplatform.learning.entity.Flashcard;
import com.example.aiplatform.learning.entity.FlashcardReview;
import com.example.aiplatform.learning.entity.FlashcardReviewResult;
import com.example.aiplatform.learning.entity.Topic;
import com.example.aiplatform.learning.repository.FlashcardRepository;
import com.example.aiplatform.learning.repository.FlashcardReviewRepository;
import com.example.aiplatform.learning.repository.TopicRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class FlashcardService {

    private final TopicRepository topicRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardReviewRepository flashcardReviewRepository;
    private final AiFlashcardClient aiFlashcardClient;
    private final TransactionTemplate transactionTemplate;

    public FlashcardService(
            TopicRepository topicRepository,
            FlashcardRepository flashcardRepository,
            FlashcardReviewRepository flashcardReviewRepository,
            AiFlashcardClient aiFlashcardClient,
            PlatformTransactionManager transactionManager
    ) {
        this.topicRepository = topicRepository;
        this.flashcardRepository = flashcardRepository;
        this.flashcardReviewRepository = flashcardReviewRepository;
        this.aiFlashcardClient = aiFlashcardClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public GenerateFlashcardsResponse generate(UUID userId, UUID topicId, GenerateFlashcardsRequest request) {
        Topic topic = getOwnedTopicWithGoal(userId, topicId);
        int count = request == null ? 5 : request.effectiveCount();

        AiGenerateFlashcardsResponse aiResponse = aiFlashcardClient.generateFlashcards(new AiGenerateFlashcardsRequest(
                topic.getLearningGoal().getTitle(),
                topic.getTitle(),
                topic.getDescription() == null ? "" : topic.getDescription(),
                topic.getDifficultyLevel(),
                count
        ));
        validateAiResponse(aiResponse);

        return Objects.requireNonNull(transactionTemplate.execute(status -> saveGeneratedFlashcards(userId, topicId, aiResponse)));
    }

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getByTopic(UUID userId, UUID topicId) {
        getOwnedTopicWithGoal(userId, topicId);
        return flashcardRepository.findAllByTopicIdAndUserIdOrderByCreatedAtAsc(topicId, userId).stream()
                .map(this::toFlashcardResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlashcardResponse getById(UUID userId, UUID flashcardId) {
        return toFlashcardResponse(getOwnedFlashcard(userId, flashcardId));
    }

    @Transactional
    public ReviewFlashcardResponse review(UUID userId, UUID flashcardId, FlashcardReviewRequest request) {
        Flashcard flashcard = getOwnedFlashcard(userId, flashcardId);
        FlashcardReview review = flashcardReviewRepository.save(new FlashcardReview(
                flashcard.getUser(),
                flashcard,
                request.result()
        ));
        Topic topic = flashcard.getTopic();
        topic.updateMasteryScore(applyReviewResult(topic.getMasteryScore(), request.result()));

        return new ReviewFlashcardResponse(
                toReviewResponse(review),
                topic.getMasteryScore()
        );
    }

    private GenerateFlashcardsResponse saveGeneratedFlashcards(
            UUID userId,
            UUID topicId,
            AiGenerateFlashcardsResponse aiResponse
    ) {
        Topic topic = getOwnedTopicWithGoal(userId, topicId);
        List<FlashcardResponse> createdFlashcards = aiResponse.flashcards().stream()
                .map(aiFlashcard -> flashcardRepository.save(new Flashcard(
                        topic.getUser(),
                        topic.getLearningGoal(),
                        topic,
                        aiFlashcard.question().trim(),
                        aiFlashcard.answer().trim(),
                        parseDifficulty(aiFlashcard.difficulty(), topic.getDifficultyLevel())
                )))
                .map(this::toFlashcardResponse)
                .toList();
        return new GenerateFlashcardsResponse(createdFlashcards);
    }

    private void validateAiResponse(AiGenerateFlashcardsResponse response) {
        if (response == null || response.flashcards() == null || response.flashcards().isEmpty()) {
            throw new InvalidAiResponseException("AI-service returned empty flashcards response");
        }
        for (AiFlashcardResponse flashcard : response.flashcards()) {
            if (flashcard == null || isBlank(flashcard.question()) || isBlank(flashcard.answer())) {
                throw new InvalidAiResponseException("AI-service returned invalid flashcard");
            }
        }
    }

    private Topic getOwnedTopicWithGoal(UUID userId, UUID topicId) {
        return topicRepository.findByIdAndUserIdWithLearningGoal(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
    }

    private Flashcard getOwnedFlashcard(UUID userId, UUID flashcardId) {
        return flashcardRepository.findByIdAndUserId(flashcardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
    }

    private int applyReviewResult(int currentScore, FlashcardReviewResult result) {
        int delta = switch (result) {
            case KNOW -> 15;
            case PARTIAL -> 5;
            case DONT_KNOW -> -10;
        };
        return Math.max(0, Math.min(100, currentScore + delta));
    }

    private DifficultyLevel parseDifficulty(String value, DifficultyLevel fallback) {
        if (value == null || value.isBlank()) {
            return fallback == null ? DifficultyLevel.MEDIUM : fallback;
        }
        try {
            return DifficultyLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return fallback == null ? DifficultyLevel.MEDIUM : fallback;
        }
    }

    private FlashcardResponse toFlashcardResponse(Flashcard flashcard) {
        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getLearningGoal().getId(),
                flashcard.getTopic().getId(),
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                flashcard.getDifficulty(),
                flashcard.getCreatedAt(),
                flashcard.getUpdatedAt()
        );
    }

    private FlashcardReviewResponse toReviewResponse(FlashcardReview review) {
        return new FlashcardReviewResponse(
                review.getId(),
                review.getFlashcard().getId(),
                review.getResult(),
                review.getReviewedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
