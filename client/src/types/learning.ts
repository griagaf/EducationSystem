export type LearningGoalType =
  | 'SELF_STUDY'
  | 'EXAM_PREPARATION'
  | 'INTERVIEW_PREPARATION'
  | 'TECHNOLOGY_LEARNING';

export type LearningGoalStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD';

export type RoadmapStepStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export type FlashcardReviewResult = 'KNOW' | 'PARTIAL' | 'DONT_KNOW';

export type StudyMaterialProcessingStatus =
  | 'UPLOADED'
  | 'TEXT_EXTRACTED'
  | 'TOPICS_EXTRACTED'
  | 'FAILED';

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

export type Topic = {
  id: string;
  learningGoalId?: string;
  title: string;
  description?: string | null;
  masteryScore: number;
  difficultyLevel: DifficultyLevel;
};

export type RoadmapStep = {
  id: string;
  topicId: string | null;
  title: string;
  description: string;
  orderIndex: number;
  estimatedDays: number;
  status: RoadmapStepStatus;
};

export type Roadmap = {
  id: string;
  learningGoalId: string;
  title: string;
  description: string;
  steps: RoadmapStep[];
  createdAt?: string;
  updatedAt?: string;
};

export type CreatedTaskSummary = {
  id: string;
  title: string;
  status: string;
  priority: string;
};

export type GenerateRoadmapRequest = {
  userLevel: 'beginner' | 'intermediate' | 'advanced';
  includeMaterials: boolean;
};

export type GenerateRoadmapResponse = {
  roadmap: Roadmap;
  topics: Topic[];
  createdTasks: CreatedTaskSummary[];
};

export type StudyMaterial = {
  id: string;
  learningGoalId: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  processingStatus: StudyMaterialProcessingStatus;
  extractedText: string | null;
  createdAt: string;
};

export type Flashcard = {
  id: string;
  learningGoalId: string;
  topicId: string;
  question: string;
  answer: string;
  difficulty: DifficultyLevel;
  createdAt: string;
  updatedAt: string;
};

export type GenerateFlashcardsRequest = {
  count: number;
};

export type GenerateFlashcardsResponse = {
  createdFlashcards: Flashcard[];
};

export type FlashcardReview = {
  id: string;
  flashcardId: string;
  result: FlashcardReviewResult;
  reviewedAt: string;
};

export type ReviewFlashcardResponse = {
  review: FlashcardReview;
  topicMasteryScore: number;
};
