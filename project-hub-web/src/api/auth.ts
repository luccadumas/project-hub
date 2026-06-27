import type { TokenResponse } from '../types';
import { api } from './client';

export interface AuthProfile {
  username: string;
  roles: string[];
}

export async function login(username: string, password: string): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/api/auth/login', { username, password });
  return data;
}

export async function logout(refreshToken: string): Promise<void> {
  await api.post('/api/auth/logout', { refreshToken });
}

export async function fetchCurrentUser(): Promise<AuthProfile> {
  const { data } = await api.get<AuthProfile>('/api/auth/me');
  return data;
}
