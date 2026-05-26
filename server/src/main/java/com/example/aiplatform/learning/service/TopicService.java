package com.example.aiplatform.learning.service;

import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.learning.dto.TopicResponse;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.learning.repository.TopicRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicService {

    private final LearningGoalRepository learningGoalRepository;
    private final TopicRepository topicRepository;

    public TopicService(LearningGoalRepository learningGoalRepository, TopicRepository topicRepository) {
        this.learningGoalRepository = learningGoalRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getByGoal(UUID userId, UUID goalId) {
        if (!learningGoalRepository.existsByIdAndUserId(goalId, userId)) {
            throw new ResourceNotFoundException("Learning goal not found");
        }

        return topicRepository.findAllByLearningGoalIdAndUserIdOrderByCreatedAtAsc(goalId, userId)
                .stream()
                .map(topic -> new TopicResponse(
                        topic.getId(),
                        topic.getLearningGoal().getId(),
                        topic.getTitle(),
                        topic.getDescription(),
                        topic.getMasteryScore(),
                        topic.getDifficultyLevel()
                ))
                .toList();
    }
}
