package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.Topic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findAllByLearningGoalIdAndUserIdOrderByCreatedAtAsc(UUID learningGoalId, UUID userId);

    List<Topic> findAllByUserIdOrderByMasteryScoreAscCreatedAtAsc(UUID userId);

    Optional<Topic> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select topic
            from Topic topic
                join fetch topic.user
                join fetch topic.learningGoal
            where topic.id = :topicId and topic.user.id = :userId
            """)
    Optional<Topic> findByIdAndUserIdWithLearningGoal(@Param("topicId") UUID topicId, @Param("userId") UUID userId);
}
