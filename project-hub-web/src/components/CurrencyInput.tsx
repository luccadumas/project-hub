import { InputAdornment, TextField } from '@mui/material';
import { useEffect, useRef, useState } from 'react';
import { formatCurrencyInput, maskCurrencyInput } from '../utils/currency';

interface CurrencyInputProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  error?: boolean;
  helperText?: string;
}

export function CurrencyInput({ label, value, onChange, error, helperText }: CurrencyInputProps) {
  const [display, setDisplay] = useState(() => formatCurrencyInput(value));
  const isInternalChange = useRef(false);

  useEffect(() => {
    if (isInternalChange.current) {
      isInternalChange.current = false;
      return;
    }

    setDisplay(formatCurrencyInput(value));
  }, [value]);

  return (
    <TextField
      fullWidth
      label={label}
      value={display}
      onChange={(event) => {
        const masked = maskCurrencyInput(event.target.value);
        isInternalChange.current = true;
        setDisplay(masked.display);
        onChange(masked.value);
      }}
      error={error}
      helperText={helperText}
      placeholder="0,00"
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start" sx={{ fontWeight: 600, color: 'text.secondary' }}>
              R$
            </InputAdornment>
          ),
          inputMode: 'numeric',
        },
      }}
    />
  );
}
