import type { LearningGoalStatus, LearningGoalType } from '../../types/learning';

export const learningGoalTypeLabels: Record<LearningGoalType, string> = {
  SELF_STUDY: 'Self study',
  EXAM_PREPARATION: 'Exam preparation',
  INTERVIEW_PREPARATION: 'Interview preparation',
  TECHNOLOGY_LEARNING: 'Technology learning',
};

export const learningGoalStatusLabels: Record<LearningGoalStatus, string> = {
  ACTIVE: 'Active',
  COMPLETED: 'Completed',
  ARCHIVED: 'Archived',
};
