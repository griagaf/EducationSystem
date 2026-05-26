import { apiClient } from './apiClient';
import type { DashboardSummary } from '../types/dashboard';

export const dashboardService = {
  async getSummary() {
    const response = await apiClient.get<DashboardSummary>('/dashboard/summary');
    return response.data;
  },
};
