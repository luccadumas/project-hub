import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { TokenResponse } from '../types';

function requireEnv(name: keyof ImportMetaEnv): string {
  const value = import.meta.env[name];
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}

export const api = axios.create({
  baseURL: requireEnv('VITE_API_URL'),
});

let unauthorizedHandler: (() => void) | null = null;
let refreshPromise: Promise<TokenResponse> | null = null;

export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler;
}

export function setAccessToken(accessToken: string) {
  api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
  localStorage.setItem('accessToken', accessToken);
}

export function setRefreshToken(refreshToken: string) {
  localStorage.setItem('refreshToken', refreshToken);
}

export function setAuthRoles(roles: string[]) {
  localStorage.setItem('authRoles', JSON.stringify(roles));
}

export function getStoredRoles(): string[] {
  const raw = localStorage.getItem('authRoles');
  if (!raw) {
    return [];
  }
  try {
    return JSON.parse(raw) as string[];
  } catch {
    return [];
  }
}

export function persistTokenResponse(tokens: TokenResponse) {
  setAccessToken(tokens.accessToken);
  setRefreshToken(tokens.refreshToken);
  localStorage.setItem('authUser', tokens.username);
  setAuthRoles(tokens.roles);
}

export function clearAuthCredentials() {
  delete api.defaults.headers.common.Authorization;
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('authUser');
  localStorage.removeItem('authRoles');
}

export function restoreAuthCredentials() {
  const accessToken = localStorage.getItem('accessToken');
  if (accessToken) {
    api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
  }
}

async function refreshAccessToken(): Promise<TokenResponse> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    throw new Error('Missing refresh token');
  }

  const { data } = await axios.post<TokenResponse>(
    `${requireEnv('VITE_API_URL')}/api/auth/refresh`,
    { refreshToken },
  );

  persistTokenResponse(data);
  return data;
}

function getRefreshPromise() {
  if (!refreshPromise) {
    refreshPromise = refreshAccessToken().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const requestUrl = originalRequest?.url ?? '';

    if (
      error.response?.status === 401
      && !originalRequest?._retry
      && !requestUrl.includes('/api/auth/login')
      && !requestUrl.includes('/api/auth/refresh')
    ) {
      originalRequest._retry = true;

      try {
        await getRefreshPromise();
        return api(originalRequest);
      } catch {
        unauthorizedHandler?.();
      }
    }

    if (error.response?.status === 401) {
      unauthorizedHandler?.();
    }

    return Promise.reject(error);
  },
);

restoreAuthCredentials();
