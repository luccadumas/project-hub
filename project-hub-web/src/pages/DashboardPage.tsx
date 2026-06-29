import CheckCircleOutlineOutlinedIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import RocketLaunchOutlinedIcon from '@mui/icons-material/RocketLaunchOutlined';
import ScheduleOutlinedIcon from '@mui/icons-material/ScheduleOutlined';
import {
  Alert,
  Box,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import { fetchPortfolioReport, fetchProjects } from '../api/projects';
import { queryKeys } from '../api/queryKeys';
import { AppLayout } from '../components/AppLayout';
import { ContentCard } from '../components/ContentCard';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { StatCard } from '../components/StatCard';
import { formatCurrency, RiskChip, StatusChip } from '../utils/formatters';

export function DashboardPage() {
  const projectsQuery = useQuery({
    queryKey: queryKeys.projects.list({ page: 0, size: 5 }),
    queryFn: () => fetchProjects({ page: 0, size: 5, sort: 'name,asc' }),
  });

  const reportQuery = useQuery({
    queryKey: queryKeys.portfolioReport.all,
    queryFn: fetchPortfolioReport,
  });

  if (projectsQuery.isLoading || reportQuery.isLoading) {
    return (
      <AppLayout>
        <LoadingState />
      </AppLayout>
    );
  }

  if (projectsQuery.isError || reportQuery.isError) {
    return (
      <AppLayout>
        <Alert severity="error">Erro ao carregar dashboard</Alert>
      </AppLayout>
    );
  }

  const report = reportQuery.data!;
  const activeProjects = (report.projectsCountByStatus.IN_PROGRESS ?? 0)
    + (report.projectsCountByStatus.PLANNED ?? 0)
    + (report.projectsCountByStatus.STARTED ?? 0);

  return (
    <AppLayout>
      <PageHeader
        title="Dashboard"
        subtitle="Visão executiva do portfólio de projetos"
      />

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <StatCard
          title="Projetos ativos"
          value={activeProjects}
          icon={<RocketLaunchOutlinedIcon />}
          accent="#2563eb"
        />
        <StatCard
          title="Membros alocados"
          value={report.uniqueAllocatedMembers}
          icon={<GroupsOutlinedIcon />}
          accent="#0f766e"
        />
        <StatCard
          title="Encerrados"
          value={report.projectsCountByStatus.COMPLETED ?? 0}
          icon={<CheckCircleOutlineOutlinedIcon />}
          accent="#059669"
        />
        <StatCard
          title="Duração média (dias)"
          value={Math.round(report.averageClosedProjectDurationDays)}
          icon={<ScheduleOutlinedIcon />}
          accent="#7c3aed"
        />
      </Box>

      <ContentCard title="Projetos recentes" subtitle="Últimas atualizações do portfólio">
        {projectsQuery.data?.content.length === 0 ? (
          <Typography color="text.secondary">Nenhum projeto encontrado</Typography>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Projeto</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Risco</TableCell>
                <TableCell align="right">Orçamento</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {projectsQuery.data?.content.map((project) => (
                <TableRow
                  key={project.id}
                  hover
                  component={RouterLink}
                  to={`/projects/${project.id}`}
                  sx={{ cursor: 'pointer', textDecoration: 'none' }}
                >
                  <TableCell>
                    <Typography sx={{ fontWeight: 600 }}>{project.name}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {project.managerName}
                    </Typography>
                  </TableCell>
                  <TableCell><StatusChip status={project.status} /></TableCell>
                  <TableCell><RiskChip risk={project.riskLevel} /></TableCell>
                  <TableCell align="right">{formatCurrency(project.totalBudget)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </ContentCard>
    </AppLayout>
  );
}
