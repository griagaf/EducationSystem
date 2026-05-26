package com.example.aiplatform.tasks.service;

import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.tasks.entity.TaskStatus;
import com.example.aiplatform.tasks.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class GoalProgressService {

    private final TaskRepository taskRepository;

    public GoalProgressService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void recalculate(LearningGoal learningGoal) {
        long totalTasks = taskRepository.countByLearningGoalId(learningGoal.getId());
        if (totalTasks == 0) {
            learningGoal.updateProgressPercent(0);
            return;
        }

        long completedTasks = taskRepository.countByLearningGoalIdAndStatus(learningGoal.getId(), TaskStatus.DONE);
        int progressPercent = (int) Math.round((completedTasks * 100.0) / totalTasks);
        learningGoal.updateProgressPercent(progressPercent);
    }
}
