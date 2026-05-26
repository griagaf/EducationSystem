import type { DifficultyLevel, LearningGoalStatus, LearningGoalType } from './learning';
import type { TaskPriority, TaskStatus } from './tasks';

export type DashboardGoal = {
  id: string;
  title: string;
  type: LearningGoalType;
  status: LearningGoalStatus;
  progressPercent: number;
  createdAt: string;
};

export type DashboardTopic = {
  id: string;
  learningGoalId: string;
  title: string;
  masteryScore: number;
  difficultyLevel: DifficultyLevel;
};

export type UpcomingTask = {
  id: string;
  learningGoalId: string;
  learningGoalTitle: string;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
};

export type DashboardSummary = {
  totalGoals: number;
  activeGoals: number;
  totalTasks: number;
  completedTasks: number;
  taskCompletionPercent: number;
  averageMasteryScore: number;
  weakTopics: DashboardTopic[];
  recentGoals: DashboardGoal[];
  upcomingTasks: UpcomingTask[];
};
