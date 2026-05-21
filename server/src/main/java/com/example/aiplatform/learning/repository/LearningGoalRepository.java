package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.LearningGoal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, UUID> {

    List<LearningGoal> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<LearningGoal> findByIdAndUserId(UUID id, UUID userId);
}
