package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.StudyMaterial;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {

    List<StudyMaterial> findAllByLearningGoalIdAndUserIdOrderByCreatedAtDesc(UUID learningGoalId, UUID userId);

    Optional<StudyMaterial> findByIdAndUserId(UUID id, UUID userId);
}
