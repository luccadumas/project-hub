import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useDebouncedValue } from './useDebouncedValue';

describe('useDebouncedValue', () => {
  it('debounces value updates', async () => {
    const { result, rerender } = renderHook(
      ({ value, delayMs }) => useDebouncedValue(value, delayMs),
      { initialProps: { value: 'a', delayMs: 100 } },
    );

    expect(result.current).toBe('a');

    rerender({ value: 'abc', delayMs: 100 });
    expect(result.current).toBe('a');

    await waitFor(() => {
      expect(result.current).toBe('abc');
    });
  });
});
