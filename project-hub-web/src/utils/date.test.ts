import { describe, expect, it } from 'vitest';
import {
  displayToIso,
  formatDate,
  isIsoDateBefore,
  maskDateInput,
  parseApiDate,
} from './date';

describe('date utils', () => {
  it('parses ISO dates from the API', () => {
    expect(parseApiDate('2025-06-15')).not.toBeNull();
    expect(parseApiDate('invalid')).toBeNull();
  });

  it('formats API dates for display', () => {
    expect(formatDate('2025-06-15')).toBe('15/06/2025');
    expect(formatDate(null)).toBe('-');
  });

  it('masks typed dates as dd/mm/yyyy', () => {
    expect(maskDateInput('15062025')).toBe('15/06/2025');
  });

  it('converts display dates back to ISO', () => {
    expect(displayToIso('15/06/2025')).toBe('2025-06-15');
    expect(displayToIso('15/06/25')).toBeNull();
  });

  it('detects when an ISO date is before another', () => {
    expect(isIsoDateBefore('2025-01-01', '2025-03-01')).toBe(true);
    expect(isIsoDateBefore('2025-03-01', '2025-01-01')).toBe(false);
    expect(isIsoDateBefore('2025-03-01', '2025-03-01')).toBe(false);
  });
});
