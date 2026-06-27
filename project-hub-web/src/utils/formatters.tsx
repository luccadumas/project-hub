import { Chip } from '@mui/material';
import type { ProjectStatus, RiskLevel } from '../types';

const STATUS_LABELS: Record<ProjectStatus, string> = {
  EM_ANALISE: 'Em analise',
  ANALISE_REALIZADA: 'Analise realizada',
  ANALISE_APROVADA: 'Analise aprovada',
  INICIADO: 'Iniciado',
  PLANEJADO: 'Planejado',
  EM_ANDAMENTO: 'Em andamento',
  ENCERRADO: 'Encerrado',
  CANCELADO: 'Cancelado',
};

const STATUS_STYLES: Record<ProjectStatus, { bg: string; color: string }> = {
  EM_ANALISE: { bg: '#eff6ff', color: '#1d4ed8' },
  ANALISE_REALIZADA: { bg: '#eef2ff', color: '#4338ca' },
  ANALISE_APROVADA: { bg: '#f0fdf4', color: '#15803d' },
  INICIADO: { bg: '#ecfeff', color: '#0e7490' },
  PLANEJADO: { bg: '#f0f9ff', color: '#0369a1' },
  EM_ANDAMENTO: { bg: '#fff7ed', color: '#c2410c' },
  ENCERRADO: { bg: '#f0fdf4', color: '#166534' },
  CANCELADO: { bg: '#fef2f2', color: '#b91c1c' },
};

const RISK_STYLES: Record<RiskLevel, { bg: string; color: string; label: string }> = {
  BAIXO: { bg: '#ecfdf5', color: '#047857', label: 'Baixo' },
  MEDIO: { bg: '#fffbeb', color: '#b45309', label: 'Medio' },
  ALTO: { bg: '#fef2f2', color: '#b91c1c', label: 'Alto' },
};

export function formatStatus(status: ProjectStatus): string {
  return STATUS_LABELS[status] ?? status;
}

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}

export function StatusChip({ status }: { status: ProjectStatus }) {
  const style = STATUS_STYLES[status];
  return (
    <Chip
      label={formatStatus(status)}
      size="small"
      sx={{
        bgcolor: style.bg,
        color: style.color,
        fontWeight: 600,
        border: 'none',
      }}
    />
  );
}

export function RiskChip({ risk }: { risk: RiskLevel }) {
  const style = RISK_STYLES[risk];
  return (
    <Chip
      label={style.label}
      size="small"
      sx={{
        bgcolor: style.bg,
        color: style.color,
        fontWeight: 600,
        border: 'none',
      }}
    />
  );
}

export const PROJECT_STATUSES: ProjectStatus[] = [
  'EM_ANALISE',
  'ANALISE_REALIZADA',
  'ANALISE_APROVADA',
  'INICIADO',
  'PLANEJADO',
  'EM_ANDAMENTO',
  'ENCERRADO',
  'CANCELADO',
];

export const RISK_LEVELS: RiskLevel[] = ['BAIXO', 'MEDIO', 'ALTO'];
