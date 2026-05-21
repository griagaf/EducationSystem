import { apiClient } from './apiClient';
import type {
  LearningGoal,
  LearningGoalCreateRequest,
} from '../types/learning';

export const learningService = {
  async getGoals() {
    const response = await apiClient.get<LearningGoal[]>('/goals');
    return response.data;
  },

  async createGoal(request: LearningGoalCreateRequest) {
    const response = await apiClient.post<LearningGoal>('/goals', request);
    return response.data;
  },

  async getGoal(goalId: string) {
    const response = await apiClient.get<LearningGoal>(`/goals/${goalId}`);
    return response.data;
  },
};
