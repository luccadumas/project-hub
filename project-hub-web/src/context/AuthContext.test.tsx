import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { persistTokenResponse } from '../api/client';
import { AuthProvider, useAuth } from './AuthContext';

function createWrapper(queryClient: QueryClient) {
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
}

describe('AuthProvider', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('clears react query cache on logout', () => {
    const queryClient = new QueryClient();
    queryClient.setQueryData(['projects'], [{ id: 1, name: 'Portal' }]);
    queryClient.setQueryData(['portfolio-report'], { totalProjects: 1 });

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      persistTokenResponse({
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresIn: 900,
        username: 'admin',
        roles: ['ADMIN'],
      });
      result.current.logout();
    });

    expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.roles).toEqual([]);
  });
});
