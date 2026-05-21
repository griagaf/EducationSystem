package com.example.aiplatform.learning.service;

import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.learning.dto.DeleteLearningGoalResponse;
import com.example.aiplatform.learning.dto.LearningGoalCreateRequest;
import com.example.aiplatform.learning.dto.LearningGoalResponse;
import com.example.aiplatform.learning.dto.LearningGoalUpdateRequest;
import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.user.entity.User;
import com.example.aiplatform.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningGoalService {

    private final LearningGoalRepository learningGoalRepository;
    private final UserRepository userRepository;

    public LearningGoalService(
            LearningGoalRepository learningGoalRepository,
            UserRepository userRepository
    ) {
        this.learningGoalRepository = learningGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LearningGoalResponse create(UUID userId, LearningGoalCreateRequest request) {
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LearningGoal goal = learningGoalRepository.save(new LearningGoal(
                user,
                request.title().trim(),
                request.description().trim(),
                request.type(),
                request.targetDate(),
                request.durationWeeks()
        ));
        return toResponse(goal);
    }

    @Transactional(readOnly = true)
    public List<LearningGoalResponse> getList(UUID userId) {
        return learningGoalRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningGoalResponse getById(UUID userId, UUID goalId) {
        return toResponse(getOwnedGoal(userId, goalId));
    }

    @Transactional
    public LearningGoalResponse update(UUID userId, UUID goalId, LearningGoalUpdateRequest request) {
        LearningGoal goal = getOwnedGoal(userId, goalId);
        goal.update(
                request.title().trim(),
                request.description().trim(),
                request.type(),
                request.status(),
                request.targetDate(),
                request.durationWeeks()
        );
        return toResponse(goal);
    }

    @Transactional
    public DeleteLearningGoalResponse archive(UUID userId, UUID goalId) {
        LearningGoal goal = getOwnedGoal(userId, goalId);
        goal.archive();
        return new DeleteLearningGoalResponse(true);
    }

    private LearningGoal getOwnedGoal(UUID userId, UUID goalId) {
        return learningGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
    }

    private LearningGoalResponse toResponse(LearningGoal goal) {
        Integer durationWeeks = goal.getEstimatedDurationWeeks();
        return new LearningGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getType(),
                goal.getStatus(),
                goal.getTargetDate(),
                durationWeeks,
                durationWeeks == null ? null : durationWeeks + " weeks",
                goal.getProgressPercent(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
