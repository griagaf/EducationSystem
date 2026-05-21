export type LearningGoalType =
  | 'SELF_STUDY'
  | 'EXAM_PREPARATION'
  | 'INTERVIEW_PREPARATION'
  | 'TECHNOLOGY_LEARNING';

export type LearningGoalStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export type LearningGoal = {
  id: string;
  title: string;
  description: string;
  type: LearningGoalType;
  status: LearningGoalStatus;
  targetDate: string | null;
  durationWeeks: number | null;
  estimatedDuration: string | null;
  progressPercent: number;
  createdAt: string;
  updatedAt: string;
};

export type LearningGoalCreateRequest = {
  title: string;
  description: string;
  type: LearningGoalType;
  targetDate: string | null;
  durationWeeks: number | null;
};
