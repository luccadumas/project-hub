import { describe, expect, it } from 'vitest';
import type { Member } from '../types';
import { filterProjectEmployees, filterProjectManagers } from './members';

describe('member filters', () => {
  const members: Member[] = [
    { id: 1, name: 'Ana', role: 'gerente' },
    { id: 2, name: 'Bruno', role: 'funcionario' },
    { id: 3, name: 'Elisa', role: 'estagiario' },
    { id: 4, name: 'Carlos', role: 'consultor' },
  ];

  it('returns only gerente members for project manager selection', () => {
    expect(filterProjectManagers(members)).toEqual([
      { id: 1, name: 'Ana', role: 'gerente' },
    ]);
  });

  it('returns only funcionario members for allocation selection', () => {
    expect(filterProjectEmployees(members)).toEqual([
      { id: 2, name: 'Bruno', role: 'funcionario' },
    ]);
  });
});
