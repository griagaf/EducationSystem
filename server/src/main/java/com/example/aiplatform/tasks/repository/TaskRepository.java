package com.example.aiplatform.tasks.repository;

import com.example.aiplatform.tasks.entity.Task;
import com.example.aiplatform.tasks.entity.TaskPriority;
import com.example.aiplatform.tasks.entity.TaskStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            select t from Task t
            where t.user.id = :userId
              and (:status is null or t.status = :status)
              and (:priority is null or t.priority = :priority)
              and (:learningGoalId is null or t.learningGoal.id = :learningGoalId)
            order by t.createdAt desc
            """)
    List<Task> findAllForUser(
            @Param("userId") UUID userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("learningGoalId") UUID learningGoalId
    );

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    long countByLearningGoalId(UUID learningGoalId);

    long countByLearningGoalIdAndStatus(UUID learningGoalId, TaskStatus status);
}
