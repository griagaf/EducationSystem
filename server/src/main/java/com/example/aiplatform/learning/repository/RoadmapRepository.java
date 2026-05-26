package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.Roadmap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    boolean existsByLearningGoalId(UUID learningGoalId);

    Optional<Roadmap> findByLearningGoalIdAndLearningGoalUserId(UUID learningGoalId, UUID userId);

    Optional<Roadmap> findByIdAndLearningGoalUserId(UUID id, UUID userId);
}
