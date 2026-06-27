export function formatCurrencyInput(value: number | undefined | null): string {
  if (value == null || Number.isNaN(value) || value <= 0) {
    return '';
  }

  return new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

export function parseCurrencyInput(raw: string): number {
  const digits = raw.replace(/\D/g, '');
  if (!digits) {
    return 0;
  }

  return Number(digits) / 100;
}

export function maskCurrencyInput(raw: string): { display: string; value: number } {
  const digits = raw.replace(/\D/g, '');

  if (!digits) {
    return { display: '', value: 0 };
  }

  const value = Number(digits) / 100;
  const display = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

  return { display, value };
}
