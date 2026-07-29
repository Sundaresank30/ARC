import { apiClient } from './client';
import { LoginRequest, LoginResponse, MeResponse } from '../types';

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/api/auth/login', request);
  return data;
}

export async function getCurrentUser(): Promise<MeResponse> {
  const { data } = await apiClient.get<MeResponse>('/api/auth/me');
  return data;
}
