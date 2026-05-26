package com.example.aiplatform.tasks.service;

import com.example.aiplatform.ai.dto.AiRoadmapStepResponse;
import com.example.aiplatform.ai.dto.AiTaskSuggestionResponse;
import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.learning.entity.RoadmapStep;
import com.example.aiplatform.learning.entity.Topic;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.learning.repository.RoadmapStepRepository;
import com.example.aiplatform.learning.repository.TopicRepository;
import com.example.aiplatform.tasks.dto.CreateTaskRequest;
import com.example.aiplatform.tasks.dto.DeleteTaskResponse;
import com.example.aiplatform.tasks.dto.TaskResponse;
import com.example.aiplatform.tasks.dto.UpdateTaskRequest;
import com.example.aiplatform.tasks.dto.UpdateTaskStatusRequest;
import com.example.aiplatform.tasks.entity.Task;
import com.example.aiplatform.tasks.entity.TaskPriority;
import com.example.aiplatform.tasks.entity.TaskStatus;
import com.example.aiplatform.tasks.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final LearningGoalRepository learningGoalRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final TopicRepository topicRepository;
    private final GoalProgressService goalProgressService;

    public TaskService(
            TaskRepository taskRepository,
            LearningGoalRepository learningGoalRepository,
            RoadmapStepRepository roadmapStepRepository,
            TopicRepository topicRepository,
            GoalProgressService goalProgressService
    ) {
        this.taskRepository = taskRepository;
        this.learningGoalRepository = learningGoalRepository;
        this.roadmapStepRepository = roadmapStepRepository;
        this.topicRepository = topicRepository;
        this.goalProgressService = goalProgressService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getList(UUID userId, TaskStatus status, TaskPriority priority, UUID learningGoalId) {
        return taskRepository.findAllForUser(userId, status, priority, learningGoalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(UUID userId, UUID taskId) {
        return toResponse(getOwnedTask(userId, taskId));
    }

    @Transactional
    public TaskResponse create(UUID userId, CreateTaskRequest request) {
        LearningGoal goal = getOwnedGoal(userId, request.learningGoalId());
        RoadmapStep roadmapStep = getOwnedRoadmapStep(userId, request.roadmapStepId(), goal);
        Topic topic = getOwnedTopic(userId, request.topicId(), goal);

        Task task = taskRepository.save(new Task(
                goal.getUser(),
                goal,
                roadmapStep,
                topic,
                request.title().trim(),
                trimToNull(request.description()),
                request.priority(),
                request.dueDate()
        ));
        goalProgressService.recalculate(goal);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = getOwnedTask(userId, taskId);
        task.update(
                request.title().trim(),
                trimToNull(request.description()),
                request.priority(),
                request.dueDate()
        );
        return toResponse(task);
    }

    @Transactional
    public DeleteTaskResponse delete(UUID userId, UUID taskId) {
        Task task = getOwnedTask(userId, taskId);
        LearningGoal goal = task.getLearningGoal();
        taskRepository.delete(task);
        taskRepository.flush();
        goalProgressService.recalculate(goal);
        return new DeleteTaskResponse(true);
    }

    @Transactional
    public TaskResponse changeStatus(UUID userId, UUID taskId, UpdateTaskStatusRequest request) {
        Task task = getOwnedTask(userId, taskId);
        task.changeStatus(request.status());
        goalProgressService.recalculate(task.getLearningGoal());
        return toResponse(task);
    }

    public List<Task> createTasksFromRoadmap(
            LearningGoal goal,
            List<RoadmapStep> roadmapSteps,
            List<AiRoadmapStepResponse> aiSteps
    ) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < roadmapSteps.size(); i++) {
            RoadmapStep roadmapStep = roadmapSteps.get(i);
            AiRoadmapStepResponse aiStep = i < aiSteps.size() ? aiSteps.get(i) : null;
            List<AiTaskSuggestionResponse> suggestions = aiStep == null || aiStep.tasks() == null
                    ? List.of()
                    : aiStep.tasks().stream()
                            .filter(this::isValidSuggestion)
                            .toList();

            if (suggestions.isEmpty()) {
                tasks.add(taskRepository.save(new Task(
                        goal.getUser(),
                        goal,
                        roadmapStep,
                        roadmapStep.getTopic(),
                        "Изучить: " + roadmapStep.getTitle(),
                        roadmapStep.getDescription(),
                        TaskPriority.MEDIUM,
                        null
                )));
                continue;
            }

            for (AiTaskSuggestionResponse suggestion : suggestions) {
                tasks.add(taskRepository.save(new Task(
                        goal.getUser(),
                        goal,
                        roadmapStep,
                        roadmapStep.getTopic(),
                        suggestion.title().trim(),
                        trimToNull(suggestion.description()),
                        parsePriority(suggestion.priority()),
                        null
                )));
            }
        }
        goalProgressService.recalculate(goal);
        return tasks;
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getLearningGoal().getId(),
                task.getLearningGoal().getTitle(),
                task.getRoadmapStep() == null ? null : task.getRoadmapStep().getId(),
                task.getTopic() == null ? null : task.getTopic().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private Task getOwnedTask(UUID userId, UUID taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private LearningGoal getOwnedGoal(UUID userId, UUID goalId) {
        return learningGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
    }

    private RoadmapStep getOwnedRoadmapStep(UUID userId, UUID roadmapStepId, LearningGoal goal) {
        if (roadmapStepId == null) {
            return null;
        }
        RoadmapStep step = roadmapStepRepository.findByIdAndRoadmapLearningGoalUserId(roadmapStepId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap step not found"));
        if (!step.getRoadmap().getLearningGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("Roadmap step not found");
        }
        return step;
    }

    private Topic getOwnedTopic(UUID userId, UUID topicId, LearningGoal goal) {
        if (topicId == null) {
            return null;
        }
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        if (!topic.getLearningGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("Topic not found");
        }
        return topic;
    }

    private TaskPriority parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return TaskPriority.MEDIUM;
        }
        try {
            return TaskPriority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return TaskPriority.MEDIUM;
        }
    }

    private boolean isValidSuggestion(AiTaskSuggestionResponse suggestion) {
        return suggestion != null && suggestion.title() != null && !suggestion.title().isBlank();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
