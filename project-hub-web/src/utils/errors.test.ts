import { describe, expect, it } from 'vitest';
import { getApiErrorMessage } from './errors';

describe('getApiErrorMessage', () => {
  it('translates known API messages to client-friendly Portuguese', () => {
    const error = {
      isAxiosError: true,
      response: {
        data: { message: 'Expected end date must be on or after start date' },
      },
    };

    expect(getApiErrorMessage(error, 'fallback')).toBe(
      'A previsão de término deve ser igual ou posterior à data de início.',
    );
  });

  it('translates member overload using member name from API message', () => {
    const error = {
      isAxiosError: true,
      response: {
        data: { message: 'Bruno Costa is already allocated to 3 active projects' },
      },
    };

    expect(getApiErrorMessage(error, 'fallback')).toBe(
      'Bruno Costa já está alocado em 3 projetos ativos. Escolha outro membro.',
    );
  });

  it('translates legacy member id overload using members context', () => {
    const error = {
      isAxiosError: true,
      response: {
        data: { message: 'Member id 2 is already allocated to 3 active projects' },
      },
    };

    expect(getApiErrorMessage(error, 'fallback', {
      members: [{ id: 2, name: 'Bruno Costa' }],
    })).toBe(
      'Bruno Costa já está alocado em 3 projetos ativos. Escolha outro membro.',
    );
  });

  it('returns fallback when API message is unavailable', () => {
    expect(getApiErrorMessage(new Error('x'), 'Não foi possível salvar.')).toBe('Não foi possível salvar.');
  });

  it('returns fallback for unknown API messages instead of raw English text', () => {
    const error = {
      isAxiosError: true,
      response: {
        data: { message: 'Some unknown technical error' },
      },
    };

    expect(getApiErrorMessage(error, 'Não foi possível salvar.')).toBe('Não foi possível salvar.');
  });
});
