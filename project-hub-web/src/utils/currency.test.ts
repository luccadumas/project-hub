import { describe, expect, it } from 'vitest';
import {
  formatCurrencyInput,
  maskCurrencyInput,
  parseCurrencyInput,
} from './currency';

describe('currency utils', () => {
  it('formats positive values in pt-BR', () => {
    expect(formatCurrencyInput(1234.5)).toBe('1.234,50');
    expect(formatCurrencyInput(0)).toBe('');
  });

  it('parses masked currency input', () => {
    expect(parseCurrencyInput('1.234,50')).toBe(1234.5);
    expect(parseCurrencyInput('')).toBe(0);
  });

  it('masks raw digits into currency display and numeric value', () => {
    expect(maskCurrencyInput('123450')).toEqual({
      display: '1.234,50',
      value: 1234.5,
    });
  });
});
