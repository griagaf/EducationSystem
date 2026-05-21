package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.RoadmapStep;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, UUID> {

    List<RoadmapStep> findAllByRoadmapIdOrderByOrderIndexAsc(UUID roadmapId);
}
