package com.example.aiplatform.dashboard.service;

import com.example.aiplatform.dashboard.dto.DashboardGoalResponse;
import com.example.aiplatform.dashboard.dto.DashboardSummaryResponse;
import com.example.aiplatform.dashboard.dto.DashboardTopicResponse;
import com.example.aiplatform.dashboard.dto.UpcomingTaskResponse;
import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.learning.entity.LearningGoalStatus;
import com.example.aiplatform.learning.entity.Topic;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.learning.repository.TopicRepository;
import com.example.aiplatform.tasks.entity.Task;
import com.example.aiplatform.tasks.entity.TaskStatus;
import com.example.aiplatform.tasks.repository.TaskRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int WEAK_TOPIC_THRESHOLD = 50;
    private static final int MAX_WEAK_TOPICS = 5;
    private static final int MAX_RECENT_GOALS = 5;
    private static final int MAX_UPCOMING_TASKS = 5;

    private final LearningGoalRepository learningGoalRepository;
    private final TaskRepository taskRepository;
    private final TopicRepository topicRepository;

    public DashboardService(
            LearningGoalRepository learningGoalRepository,
            TaskRepository taskRepository,
            TopicRepository topicRepository
    ) {
        this.learningGoalRepository = learningGoalRepository;
        this.taskRepository = taskRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UUID userId) {
        long totalGoals = learningGoalRepository.countByUserId(userId);
        long activeGoals = learningGoalRepository.countByUserIdAndStatus(userId, LearningGoalStatus.ACTIVE);
        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);
        List<Topic> topics = topicRepository.findAllByUserIdOrderByMasteryScoreAscCreatedAtAsc(userId);

        return new DashboardSummaryResponse(
                totalGoals,
                activeGoals,
                totalTasks,
                completedTasks,
                calculatePercent(completedTasks, totalTasks),
                calculateAverageMastery(topics),
                weakTopics(topics),
                recentGoals(userId),
                upcomingTasks(userId)
        );
    }

    private int calculatePercent(long value, long total) {
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((double) value * 100 / total);
    }

    private int calculateAverageMastery(List<Topic> topics) {
        if (topics.isEmpty()) {
            return 0;
        }
        return (int) Math.round(topics.stream()
                .mapToInt(Topic::getMasteryScore)
                .average()
                .orElse(0));
    }

    private List<DashboardTopicResponse> weakTopics(List<Topic> topics) {
        return topics.stream()
                .filter(topic -> topic.getMasteryScore() < WEAK_TOPIC_THRESHOLD)
                .limit(MAX_WEAK_TOPICS)
                .map(this::toTopicResponse)
                .toList();
    }

    private List<DashboardGoalResponse> recentGoals(UUID userId) {
        return learningGoalRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(MAX_RECENT_GOALS)
                .map(this::toGoalResponse)
                .toList();
    }

    private List<UpcomingTaskResponse> upcomingTasks(UUID userId) {
        return taskRepository.findUpcomingForUser(userId, List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS)).stream()
                .limit(MAX_UPCOMING_TASKS)
                .map(this::toTaskResponse)
                .toList();
    }

    private DashboardTopicResponse toTopicResponse(Topic topic) {
        return new DashboardTopicResponse(
                topic.getId(),
                topic.getLearningGoal().getId(),
                topic.getTitle(),
                topic.getMasteryScore(),
                topic.getDifficultyLevel()
        );
    }

    private DashboardGoalResponse toGoalResponse(LearningGoal goal) {
        return new DashboardGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getType(),
                goal.getStatus(),
                goal.getProgressPercent(),
                goal.getCreatedAt()
        );
    }

    private UpcomingTaskResponse toTaskResponse(Task task) {
        return new UpcomingTaskResponse(
                task.getId(),
                task.getLearningGoal().getId(),
                task.getLearningGoal().getTitle(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate()
        );
    }
}
