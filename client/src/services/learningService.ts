import { apiClient } from './apiClient';
import axios from 'axios';
import type {
  GenerateRoadmapRequest,
  GenerateRoadmapResponse,
  LearningGoal,
  LearningGoalCreateRequest,
  Roadmap,
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

  async getRoadmap(goalId: string) {
    try {
      const response = await apiClient.get<Roadmap>(`/goals/${goalId}/roadmap`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async generateRoadmap(
    goalId: string,
    request: GenerateRoadmapRequest = {
      userLevel: 'beginner',
      includeMaterials: false,
    },
  ) {
    const response = await apiClient.post<GenerateRoadmapResponse>(
      `/goals/${goalId}/generate-roadmap`,
      request,
    );
    return response.data;
  },
};
