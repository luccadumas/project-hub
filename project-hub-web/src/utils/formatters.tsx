import { Chip } from '@mui/material';
import type { MemberRole, ProjectStatus, RiskLevel } from '../types';

const STATUS_LABELS: Record<ProjectStatus, string> = {
  UNDER_ANALYSIS: 'Em análise',
  ANALYSIS_COMPLETED: 'Análise realizada',
  ANALYSIS_APPROVED: 'Análise aprovada',
  STARTED: 'Iniciado',
  PLANNED: 'Planejado',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Encerrado',
  CANCELED: 'Cancelado',
};

const STATUS_STYLES: Record<ProjectStatus, { bg: string; color: string }> = {
  UNDER_ANALYSIS: { bg: '#eff6ff', color: '#1d4ed8' },
  ANALYSIS_COMPLETED: { bg: '#eef2ff', color: '#4338ca' },
  ANALYSIS_APPROVED: { bg: '#f0fdf4', color: '#15803d' },
  STARTED: { bg: '#ecfeff', color: '#0e7490' },
  PLANNED: { bg: '#f0f9ff', color: '#0369a1' },
  IN_PROGRESS: { bg: '#fff7ed', color: '#c2410c' },
  COMPLETED: { bg: '#f0fdf4', color: '#166534' },
  CANCELED: { bg: '#fef2f2', color: '#b91c1c' },
};

const RISK_STYLES: Record<RiskLevel, { bg: string; color: string; label: string }> = {
  LOW: { bg: '#ecfdf5', color: '#047857', label: 'Baixo' },
  MEDIUM: { bg: '#fffbeb', color: '#b45309', label: 'Médio' },
  HIGH: { bg: '#fef2f2', color: '#b91c1c', label: 'Alto' },
};

const MEMBER_ROLE_LABELS: Record<MemberRole, string> = {
  manager: 'Gerente',
  employee: 'Funcionário',
  intern: 'Estagiário',
  consultant: 'Consultor',
};

export function formatMemberRole(role: MemberRole): string {
  return MEMBER_ROLE_LABELS[role] ?? role;
}

export function formatMemberLabel(name: string, role: MemberRole): string {
  return `${name} (${formatMemberRole(role)})`;
}

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
  'UNDER_ANALYSIS',
  'ANALYSIS_COMPLETED',
  'ANALYSIS_APPROVED',
  'STARTED',
  'PLANNED',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELED',
];

export const RISK_LEVELS: RiskLevel[] = ['LOW', 'MEDIUM', 'HIGH'];
