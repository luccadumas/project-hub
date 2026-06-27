import { format, isValid, parseISO } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export const DATE_DISPLAY_FORMAT = 'dd/MM/yyyy';
export const DATE_LONG_FORMAT = "dd 'de' MMMM 'de' yyyy";
export const DATE_API_FORMAT = 'yyyy-MM-dd';

export function parseApiDate(value: string | Date | null | undefined): Date | null {
  if (!value) {
    return null;
  }

  if (value instanceof Date) {
    return isValid(value) ? value : null;
  }

  const normalized = value.length >= 10 ? value.slice(0, 10) : value;
  const parsed = parseISO(normalized);

  return isValid(parsed) ? parsed : null;
}

export function formatDate(
  value: string | Date | null | undefined,
  pattern: string = DATE_DISPLAY_FORMAT,
): string {
  const date = parseApiDate(value);

  if (!date) {
    return '-';
  }

  return format(date, pattern, { locale: ptBR });
}

export function formatDateLong(value: string | Date | null | undefined): string {
  return formatDate(value, DATE_LONG_FORMAT);
}

export function isoToDisplay(value: string | null | undefined): string {
  if (!value) {
    return '';
  }

  const date = parseApiDate(value);
  if (!date) {
    return '';
  }

  return format(date, DATE_DISPLAY_FORMAT, { locale: ptBR });
}

export function maskDateInput(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 8);

  if (digits.length <= 2) {
    return digits;
  }

  if (digits.length <= 4) {
    return `${digits.slice(0, 2)}/${digits.slice(2)}`;
  }

  return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
}

export function displayToIso(display: string): string | null {
  const digits = display.replace(/\D/g, '');

  if (digits.length !== 8) {
    return null;
  }

  const day = digits.slice(0, 2);
  const month = digits.slice(2, 4);
  const year = digits.slice(4, 8);
  const iso = `${year}-${month}-${day}`;

  return parseApiDate(iso) ? iso : null;
}

export function toApiDate(value: string | null | undefined): string | null {
  if (!value) {
    return null;
  }

  const fromDisplay = displayToIso(value);
  if (fromDisplay) {
    return fromDisplay;
  }

  const fromIso = parseApiDate(value);
  return fromIso ? format(fromIso, DATE_API_FORMAT) : null;
}
