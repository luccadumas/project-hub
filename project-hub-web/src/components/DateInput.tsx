import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { DATE_DISPLAY_FORMAT, dateToIso, parseApiDate } from '../utils/date';

interface DateInputProps {
  label: string;
  value: string;
  onChange: (isoValue: string) => void;
  error?: boolean;
  helperText?: string;
  minDate?: string;
  maxDate?: string;
}

export function DateInput({
  label,
  value,
  onChange,
  error,
  helperText,
  minDate,
  maxDate,
}: DateInputProps) {
  const selectedDate = parseApiDate(value);
  const minimumDate = parseApiDate(minDate) ?? undefined;
  const maximumDate = parseApiDate(maxDate) ?? undefined;

  return (
    <DatePicker
      label={label}
      format={DATE_DISPLAY_FORMAT}
      value={selectedDate}
      minDate={minimumDate}
      maxDate={maximumDate}
      onChange={(date) => onChange(dateToIso(date))}
      slots={{
        openPickerIcon: CalendarMonthOutlinedIcon,
      }}
      slotProps={{
        textField: {
          fullWidth: true,
          error,
          helperText: helperText ?? 'Clique no ícone para selecionar a data',
          slotProps: {
            formHelperText: { sx: { minHeight: '1.25em' } },
          },
        },
        openPickerButton: {
          'aria-label': `Abrir calendário para ${label}`,
        },
      }}
    />
  );
}
