import { beforeEach, describe, expect, it } from 'vitest';
import {
  clearAuthCredentials,
  getStoredRoles,
  persistTokenResponse,
} from './client';

describe('auth client storage', () => {
  beforeEach(() => {
    localStorage.clear();
    clearAuthCredentials();
  });

  it('stores jwt tokens and roles in localStorage', () => {
    persistTokenResponse({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
      username: 'admin',
      roles: ['ADMIN'],
    });

    expect(localStorage.getItem('accessToken')).toBe('access-token');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-token');
    expect(localStorage.getItem('authUser')).toBe('admin');
    expect(getStoredRoles()).toEqual(['ADMIN']);
  });

  it('clears stored auth data on logout', () => {
    persistTokenResponse({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
      username: 'admin',
      roles: ['ADMIN'],
    });

    clearAuthCredentials();

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('authUser')).toBeNull();
    expect(getStoredRoles()).toEqual([]);
  });
});
