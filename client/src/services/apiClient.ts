import axios, { AxiosError } from 'axios';

import { tokenStorage } from './tokenStorage';
import type { ApiError } from '../types/api';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      window.dispatchEvent(new Event('auth:unauthorized'));
    }
    return Promise.reject(error);
  },
);

export function getApiErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiError>(error)) {
    const details = error.response?.data?.details;
    const firstDetail = details ? Object.values(details)[0] : undefined;
    return firstDetail ?? error.response?.data?.message ?? 'Ошибка запроса';
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Не удалось выполнить запрос';
}
