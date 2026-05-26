package com.example.aiplatform.learning.service;

import com.example.aiplatform.ai.dto.AiGenerateRoadmapRequest;
import com.example.aiplatform.ai.dto.AiGenerateRoadmapResponse;
import com.example.aiplatform.ai.dto.AiRoadmapStepResponse;
import com.example.aiplatform.ai.service.AiRoadmapClient;
import com.example.aiplatform.common.exception.InvalidAiResponseException;
import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.common.exception.RoadmapAlreadyExistsException;
import com.example.aiplatform.learning.dto.CreatedTaskResponse;
import com.example.aiplatform.learning.dto.GenerateRoadmapRequest;
import com.example.aiplatform.learning.dto.GenerateRoadmapResponse;
import com.example.aiplatform.learning.dto.RoadmapResponse;
import com.example.aiplatform.learning.dto.RoadmapStepResponse;
import com.example.aiplatform.learning.dto.TopicResponse;
import com.example.aiplatform.learning.entity.DifficultyLevel;
import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.learning.entity.Roadmap;
import com.example.aiplatform.learning.entity.RoadmapStep;
import com.example.aiplatform.learning.entity.Topic;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.learning.repository.RoadmapRepository;
import com.example.aiplatform.learning.repository.RoadmapStepRepository;
import com.example.aiplatform.learning.repository.TopicRepository;
import com.example.aiplatform.tasks.entity.Task;
import com.example.aiplatform.tasks.service.TaskService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RoadmapGenerationService {

    private final LearningGoalRepository learningGoalRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final TopicRepository topicRepository;
    private final AiRoadmapClient aiRoadmapClient;
    private final TaskService taskService;
    private final TransactionTemplate transactionTemplate;

    public RoadmapGenerationService(
            LearningGoalRepository learningGoalRepository,
            RoadmapRepository roadmapRepository,
            RoadmapStepRepository roadmapStepRepository,
            TopicRepository topicRepository,
            AiRoadmapClient aiRoadmapClient,
            TaskService taskService,
            PlatformTransactionManager transactionManager
    ) {
        this.learningGoalRepository = learningGoalRepository;
        this.roadmapRepository = roadmapRepository;
        this.roadmapStepRepository = roadmapStepRepository;
        this.topicRepository = topicRepository;
        this.aiRoadmapClient = aiRoadmapClient;
        this.taskService = taskService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public GenerateRoadmapResponse generate(UUID userId, UUID goalId, GenerateRoadmapRequest request) {
        LearningGoal goal = getOwnedGoal(userId, goalId);
        ensureRoadmapDoesNotExist(goalId);

        AiGenerateRoadmapResponse aiResponse = aiRoadmapClient.generateRoadmap(new AiGenerateRoadmapRequest(
                goal.getTitle(),
                goal.getDescription(),
                goal.getType(),
                goal.getTargetDate() == null ? null : goal.getTargetDate().toString(),
                goal.getEstimatedDurationWeeks(),
                request == null ? "beginner" : request.effectiveUserLevel()
        ));

        validateAiResponse(aiResponse);

        return Objects.requireNonNull(transactionTemplate.execute(status -> saveGeneratedRoadmap(userId, goalId, aiResponse)));
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getByGoalId(UUID userId, UUID goalId) {
        Roadmap roadmap = roadmapRepository.findByLearningGoalIdAndLearningGoalUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));
        return toRoadmapResponse(roadmap);
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getById(UUID userId, UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findByIdAndLearningGoalUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));
        return toRoadmapResponse(roadmap);
    }

    private GenerateRoadmapResponse saveGeneratedRoadmap(UUID userId, UUID goalId, AiGenerateRoadmapResponse aiResponse) {
        LearningGoal goal = getOwnedGoal(userId, goalId);
        ensureRoadmapDoesNotExist(goalId);

        Roadmap roadmap = roadmapRepository.save(new Roadmap(
                goal,
                aiResponse.roadmapTitle().trim(),
                aiResponse.roadmapDescription().trim()
        ));

        Map<String, Topic> topicsByTitle = saveTopics(goal, aiResponse.steps());
        List<RoadmapStep> steps = new ArrayList<>();
        for (AiRoadmapStepResponse aiStep : aiResponse.steps()) {
            String topicTitle = normalizedTopics(aiStep).getFirst();
            Topic topic = topicsByTitle.get(topicTitle);
            RoadmapStep step = roadmapStepRepository.save(new RoadmapStep(
                    roadmap,
                    topic,
                    aiStep.title().trim(),
                    aiStep.description().trim(),
                    aiStep.orderIndex(),
                    aiStep.estimatedDays()
            ));
            steps.add(step);
        }

        RoadmapResponse roadmapResponse = toRoadmapResponse(roadmap, steps);
        List<TopicResponse> topicResponses = topicsByTitle.values().stream()
                .map(this::toTopicResponse)
                .toList();
        List<Task> createdTasks = taskService.createTasksFromRoadmap(goal, steps, aiResponse.steps());
        List<CreatedTaskResponse> createdTaskResponses = createdTasks.stream()
                .map(task -> new CreatedTaskResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getStatus().name(),
                        task.getPriority().name()
                ))
                .toList();
        return new GenerateRoadmapResponse(roadmapResponse, topicResponses, createdTaskResponses);
    }

    private Map<String, Topic> saveTopics(LearningGoal goal, List<AiRoadmapStepResponse> aiSteps) {
        Map<String, Topic> topicsByTitle = new LinkedHashMap<>();
        for (AiRoadmapStepResponse step : aiSteps) {
            for (String topicTitle : normalizedTopics(step)) {
                topicsByTitle.computeIfAbsent(topicTitle, title -> topicRepository.save(new Topic(
                        goal.getUser(),
                        goal,
                        title,
                        "Тема сгенерирована на основе roadmap step: " + step.title().trim(),
                        DifficultyLevel.MEDIUM
                )));
            }
        }
        return topicsByTitle;
    }

    private LearningGoal getOwnedGoal(UUID userId, UUID goalId) {
        return learningGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
    }

    private void ensureRoadmapDoesNotExist(UUID goalId) {
        if (roadmapRepository.existsByLearningGoalId(goalId)) {
            throw new RoadmapAlreadyExistsException("Roadmap already exists for this learning goal");
        }
    }

    private void validateAiResponse(AiGenerateRoadmapResponse response) {
        if (response == null) {
            throw new InvalidAiResponseException("AI-service returned empty response");
        }
        if (isBlank(response.roadmapTitle()) || isBlank(response.roadmapDescription())) {
            throw new InvalidAiResponseException("AI-service returned roadmap without title or description");
        }
        if (response.steps() == null || response.steps().isEmpty()) {
            throw new InvalidAiResponseException("AI-service returned roadmap without steps");
        }

        for (AiRoadmapStepResponse step : response.steps()) {
            if (step == null || isBlank(step.title()) || isBlank(step.description())) {
                throw new InvalidAiResponseException("AI-service returned invalid roadmap step");
            }
            if (step.orderIndex() == null || step.orderIndex() < 1) {
                throw new InvalidAiResponseException("AI-service returned roadmap step without valid order index");
            }
            if (step.estimatedDays() == null || step.estimatedDays() < 1) {
                throw new InvalidAiResponseException("AI-service returned roadmap step without valid estimated days");
            }
            if (normalizedTopics(step).isEmpty()) {
                throw new InvalidAiResponseException("AI-service returned roadmap step without topics");
            }
        }
    }

    private List<String> normalizedTopics(AiRoadmapStepResponse step) {
        if (step.topics() == null) {
            return List.of();
        }
        return step.topics().stream()
                .filter(topic -> !isBlank(topic))
                .map(String::trim)
                .distinct()
                .toList();
    }

    private RoadmapResponse toRoadmapResponse(Roadmap roadmap) {
        List<RoadmapStep> steps = roadmapStepRepository.findAllByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
        return toRoadmapResponse(roadmap, steps);
    }

    private RoadmapResponse toRoadmapResponse(Roadmap roadmap, List<RoadmapStep> steps) {
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getLearningGoal().getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                steps.stream().map(this::toRoadmapStepResponse).toList(),
                roadmap.getCreatedAt(),
                roadmap.getUpdatedAt()
        );
    }

    private RoadmapStepResponse toRoadmapStepResponse(RoadmapStep step) {
        return new RoadmapStepResponse(
                step.getId(),
                step.getTopic() == null ? null : step.getTopic().getId(),
                step.getTitle(),
                step.getDescription(),
                step.getOrderIndex(),
                step.getEstimatedDays(),
                step.getStatus()
        );
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return new TopicResponse(
                topic.getId(),
                topic.getLearningGoal().getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getMasteryScore(),
                topic.getDifficultyLevel()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
