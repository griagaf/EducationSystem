import { apiClient } from './apiClient';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../types/auth';

export const authService = {
  async login(request: LoginRequest) {
    const response = await apiClient.post<AuthResponse>('/auth/login', request);
    return response.data;
  },

  async register(request: RegisterRequest) {
    const response = await apiClient.post<AuthResponse>('/auth/register', request);
    return response.data;
  },

  async getCurrentUser() {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },
};
