import type { DifficultyLevel, FlashcardReviewResult } from '../../types/learning';

export const difficultyLabels: Record<DifficultyLevel, string> = {
  EASY: 'Easy',
  MEDIUM: 'Medium',
  HARD: 'Hard',
};

export const flashcardReviewLabels: Record<FlashcardReviewResult, string> = {
  KNOW: 'KNOW',
  PARTIAL: 'PARTIAL',
  DONT_KNOW: 'DONT_KNOW',
};
