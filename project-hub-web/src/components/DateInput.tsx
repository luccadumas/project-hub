import { TextField } from '@mui/material';
import { useEffect, useRef, useState } from 'react';
import { displayToIso, isoToDisplay, maskDateInput } from '../utils/date';

interface DateInputProps {
  label: string;
  value: string;
  onChange: (isoValue: string) => void;
  error?: boolean;
  helperText?: string;
}

export function DateInput({ label, value, onChange, error, helperText }: DateInputProps) {
  const [display, setDisplay] = useState(() => isoToDisplay(value));
  const isInternalChange = useRef(false);

  useEffect(() => {
    if (isInternalChange.current) {
      isInternalChange.current = false;
      return;
    }

    setDisplay(isoToDisplay(value));
  }, [value]);

  return (
    <TextField
      fullWidth
      label={label}
      value={display}
      onChange={(event) => {
        const masked = maskDateInput(event.target.value);
        setDisplay(masked);

        const iso = displayToIso(masked);
        isInternalChange.current = true;

        if (iso) {
          onChange(iso);
          return;
        }

        if (!masked) {
          onChange('');
        }
      }}
      error={error}
      helperText={helperText ?? 'Formato: dd/mm/aaaa'}
      placeholder="dd/mm/aaaa"
      slotProps={{
        input: {
          inputMode: 'numeric',
        },
      }}
    />
  );
}
