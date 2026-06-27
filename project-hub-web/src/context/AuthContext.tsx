import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { fetchCurrentUser, login as loginRequest, logout as logoutRequest } from '../api/auth';
import {
  clearAuthCredentials,
  getStoredRoles,
  persistTokenResponse,
  restoreAuthCredentials,
  setUnauthorizedHandler,
} from '../api/client';

interface AuthContextValue {
  username: string | null;
  roles: string[];
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [username, setUsername] = useState<string | null>(localStorage.getItem('authUser'));
  const [roles, setRoles] = useState<string[]>(getStoredRoles());
  const [isLoading, setIsLoading] = useState(Boolean(localStorage.getItem('accessToken')));

  const logout = useCallback(() => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      void logoutRequest(refreshToken).catch(() => undefined);
    }
    clearAuthCredentials();
    queryClient.clear();
    setUsername(null);
    setRoles([]);
  }, [queryClient]);

  const login = useCallback(async (user: string, password: string) => {
    const tokens = await loginRequest(user, password);
    persistTokenResponse(tokens);
    setUsername(tokens.username);
    setRoles(tokens.roles);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => logout());
    return () => setUnauthorizedHandler(() => undefined);
  }, [logout]);

  useEffect(() => {
    if (!localStorage.getItem('accessToken')) {
      setIsLoading(false);
      return;
    }

    restoreAuthCredentials();
    fetchCurrentUser()
      .then((profile) => {
        setUsername(profile.username);
        setRoles(profile.roles);
      })
      .catch(() => logout())
      .finally(() => setIsLoading(false));
  }, [logout]);

  const value = useMemo<AuthContextValue>(() => ({
    username,
    roles,
    isAuthenticated: Boolean(username),
    isAdmin: roles.includes('ADMIN'),
    isLoading,
    login,
    logout,
  }), [username, roles, isLoading, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
