import { apiClient } from './apiClient';
import type { Task, TaskFilters, TaskStatus } from '../types/tasks';

export const taskService = {
  async getTasks(filters: TaskFilters = {}) {
    const response = await apiClient.get<Task[]>('/tasks', {
      params: filters,
    });
    return response.data;
  },

  async changeStatus(taskId: string, status: TaskStatus) {
    const response = await apiClient.patch<Task>(`/tasks/${taskId}/status`, {
      status,
    });
    return response.data;
  },
};
