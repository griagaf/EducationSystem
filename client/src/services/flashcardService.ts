import { apiClient } from './apiClient';
import type {
  Flashcard,
  FlashcardReviewResult,
  GenerateFlashcardsRequest,
  GenerateFlashcardsResponse,
  ReviewFlashcardResponse,
} from '../types/learning';

export const flashcardService = {
  async getByTopic(topicId: string) {
    const response = await apiClient.get<Flashcard[]>(`/topics/${topicId}/flashcards`);
    return response.data;
  },

  async generate(topicId: string, request: GenerateFlashcardsRequest = { count: 5 }) {
    const response = await apiClient.post<GenerateFlashcardsResponse>(
      `/topics/${topicId}/generate-flashcards`,
      request,
    );
    return response.data;
  },

  async review(flashcardId: string, result: FlashcardReviewResult) {
    const response = await apiClient.post<ReviewFlashcardResponse>(
      `/flashcards/${flashcardId}/review`,
      { result },
    );
    return response.data;
  },
};
