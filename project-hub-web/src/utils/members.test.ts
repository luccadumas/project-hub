import { describe, expect, it } from 'vitest';
import { filterProjectEmployees, filterProjectManagers } from './members';

const members = [
  { id: 1, name: 'Ana', role: 'manager' as const },
  { id: 2, name: 'Bruno', role: 'employee' as const },
  { id: 3, name: 'Elisa', role: 'intern' as const },
  { id: 4, name: 'Carlos', role: 'consultant' as const },
];

describe('member filters', () => {
  it('returns only manager members for project manager selection', () => {
    expect(filterProjectManagers(members)).toEqual([
      { id: 1, name: 'Ana', role: 'manager' },
    ]);
  });

  it('returns only employee members for allocation selection', () => {
    expect(filterProjectEmployees(members)).toEqual([
      { id: 2, name: 'Bruno', role: 'employee' },
    ]);
  });
});
