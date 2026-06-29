import AccountBalanceWalletOutlinedIcon from '@mui/icons-material/AccountBalanceWalletOutlined';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import ScheduleOutlinedIcon from '@mui/icons-material/ScheduleOutlined';
import { Alert, Box, LinearProgress, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { fetchPortfolioReport } from '../api/projects';
import { queryKeys } from '../api/queryKeys';
import { AppLayout } from '../components/AppLayout';
import { ContentCard } from '../components/ContentCard';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { StatCard } from '../components/StatCard';
import { formatCurrency, formatStatus, PROJECT_STATUSES } from '../utils/formatters';

export function ReportPage() {
  const reportQuery = useQuery({
    queryKey: queryKeys.portfolioReport.all,
    queryFn: fetchPortfolioReport,
  });

  if (reportQuery.isLoading) {
    return (
      <AppLayout>
        <LoadingState />
      </AppLayout>
    );
  }

  if (reportQuery.isError || !reportQuery.data) {
    return (
      <AppLayout>
        <Alert severity="error">Erro ao carregar relatório</Alert>
      </AppLayout>
    );
  }

  const report = reportQuery.data;
  const totalProjects = Object.values(report.projectsCountByStatus).reduce((a, b) => a + b, 0);
  const totalBudget = Object.values(report.totalBudgetByStatus).reduce((a, b) => a + Number(b), 0);

  return (
    <AppLayout>
      <PageHeader
        title="Relatório do Portfólio"
        subtitle="Indicadores consolidados por status e orçamento"
      />

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <StatCard
          title="Membros únicos alocados"
          value={report.uniqueAllocatedMembers}
          icon={<GroupsOutlinedIcon />}
          accent="#0f766e"
        />
        <StatCard
          title="Duração média (dias)"
          value={Math.round(report.averageClosedProjectDurationDays)}
          icon={<ScheduleOutlinedIcon />}
          accent="#7c3aed"
        />
        <StatCard
          title="Total de projetos"
          value={totalProjects}
          icon={<AccountBalanceWalletOutlinedIcon />}
          accent="#2563eb"
        />
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' } }}>
        {PROJECT_STATUSES.map((status) => {
          const count = report.projectsCountByStatus[status] ?? 0;
          const budget = Number(report.totalBudgetByStatus[status] ?? 0);
          const share = totalProjects > 0 ? (count / totalProjects) * 100 : 0;

          return (
            <ContentCard key={status} title={formatStatus(status)}>
              <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
                {count}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {formatCurrency(budget)} orçados
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <LinearProgress
                  variant="determinate"
                  value={share}
                  sx={{
                    flex: 1,
                    height: 8,
                    borderRadius: 99,
                    bgcolor: '#e2e8f0',
                    '& .MuiLinearProgress-bar': { borderRadius: 99, bgcolor: '#2563eb' },
                  }}
                />
                <Typography variant="caption" color="text.secondary" sx={{ minWidth: 36 }}>
                  {share.toFixed(0)}%
                </Typography>
              </Box>
            </ContentCard>
          );
        })}
      </Box>

      <Box sx={{ mt: 2 }}>
        <ContentCard title="Resumo financeiro">
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            {formatCurrency(totalBudget)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Valor total orçado em todos os status
          </Typography>
        </ContentCard>
      </Box>
    </AppLayout>
  );
}
