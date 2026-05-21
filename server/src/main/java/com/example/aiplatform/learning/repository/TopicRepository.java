package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.Topic;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findAllByLearningGoalIdAndUserIdOrderByCreatedAtAsc(UUID learningGoalId, UUID userId);
}
