package com.example.aiplatform.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long totalGoals,
        long activeGoals,
        long totalTasks,
        long completedTasks,
        int taskCompletionPercent,
        int averageMasteryScore,
        List<DashboardTopicResponse> weakTopics,
        List<DashboardGoalResponse> recentGoals,
        List<UpcomingTaskResponse> upcomingTasks
) {
}
