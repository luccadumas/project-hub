import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  Divider,
  FormControlLabel,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link as RouterLink, useParams } from 'react-router-dom';
import {
  allocateMembers,
  fetchExternalMembers,
  fetchProject,
  updateProjectStatus,
} from '../api/projects';
import { queryKeys } from '../api/queryKeys';
import { AppLayout } from '../components/AppLayout';
import { ContentCard } from '../components/ContentCard';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import type { ProjectStatus } from '../types';
import {
  formatCurrency,
  formatMemberLabel,
  formatStatus,
  PROJECT_STATUSES,
  RiskChip,
  StatusChip,
} from '../utils/formatters';
import { formatDate } from '../utils/date';
import { filterProjectEmployees } from '../utils/members';
import { getApiErrorMessage } from '../utils/errors';

function InfoRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Box sx={{ py: 1.5, display: 'flex', justifyContent: 'space-between', gap: 2 }}>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
      <Typography variant="body2" sx={{ fontWeight: 600, textAlign: 'right' }}>{value}</Typography>
    </Box>
  );
}

export function ProjectDetailPage() {
  const { id } = useParams();
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [nextStatus, setNextStatus] = useState<ProjectStatus>('UNDER_ANALYSIS');
  const [selectedMembers, setSelectedMembers] = useState<number[]>([]);

  const invalidateProjectData = () => {
    if (id) {
      queryClient.invalidateQueries({ queryKey: queryKeys.project.detail(id) });
    }
    queryClient.invalidateQueries({ queryKey: queryKeys.projects.all });
    queryClient.invalidateQueries({ queryKey: queryKeys.portfolioReport.all });
  };

  const projectQuery = useQuery({
    queryKey: queryKeys.project.detail(id ?? ''),
    queryFn: () => fetchProject(Number(id)),
  });

  const membersQuery = useQuery({
    queryKey: queryKeys.externalMembers.all,
    queryFn: fetchExternalMembers,
  });

  const statusMutation = useMutation({
    mutationFn: (status: ProjectStatus) => updateProjectStatus(Number(id), status),
    onSuccess: invalidateProjectData,
  });

  const membersMutation = useMutation({
    mutationFn: (memberIds: number[]) => allocateMembers(Number(id), memberIds),
    onSuccess: invalidateProjectData,
  });

  useEffect(() => {
    if (projectQuery.data) {
      setNextStatus(projectQuery.data.status);
    }
  }, [projectQuery.data]);

  if (projectQuery.isLoading) {
    return (
      <AppLayout>
        <LoadingState />
      </AppLayout>
    );
  }

  if (projectQuery.isError || !projectQuery.data) {
    return (
      <AppLayout>
        <Alert severity="error">Projeto não encontrado</Alert>
      </AppLayout>
    );
  }

  const project = projectQuery.data;
  const employees = filterProjectEmployees(membersQuery.data ?? []);

  return (
    <AppLayout>
      <PageHeader
        title={project.name}
        subtitle="Detalhes, status e alocação de equipe"
        action={
          isAdmin ? (
            <Button
              variant="contained"
              startIcon={<EditOutlinedIcon />}
              component={RouterLink}
              to={`/projects/${project.id}/edit`}
            >
              Editar
            </Button>
          ) : undefined
        }
      />

      <Box sx={{ display: 'flex', gap: 1, mb: 3 }}>
        <StatusChip status={project.status} />
        <RiskChip risk={project.riskLevel} />
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { md: '1.6fr 1fr' } }}>
        <ContentCard title="Informações do projeto">
          <Typography color="text.secondary" sx={{ mb: 2, lineHeight: 1.7 }}>
            {project.description || 'Sem descrição cadastrada.'}
          </Typography>
          <Divider />
          <InfoRow label="Gerente responsável" value={project.managerName} />
          <Divider />
          <InfoRow label="Orçamento total" value={formatCurrency(project.totalBudget)} />
          <Divider />
          <InfoRow label="Data de início" value={formatDate(project.startDate)} />
          <Divider />
          <InfoRow label="Previsão de término" value={formatDate(project.expectedEndDate)} />
          {project.actualEndDate && (
            <>
              <Divider />
              <InfoRow label="Término real" value={formatDate(project.actualEndDate)} />
            </>
          )}
          <Divider sx={{ my: 2 }} />
          <Typography variant="subtitle2" sx={{ mb: 1 }}>Membros alocados</Typography>
          {project.members.length === 0 ? (
            <Typography color="text.secondary" variant="body2">Nenhum membro alocado</Typography>
          ) : (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {project.members.map((member) => (
                <Chip
                  key={member.id}
                  label={formatMemberLabel(member.name, member.role)}
                  sx={{ bgcolor: '#f1f5f9', fontWeight: 500 }}
                />
              ))}
            </Box>
          )}
        </ContentCard>

        {isAdmin && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <ContentCard title="Alterar status">
              <TextField
                select
                fullWidth
                label="Novo status"
                value={nextStatus}
                onChange={(e) => setNextStatus(e.target.value as ProjectStatus)}
              >
                {PROJECT_STATUSES.map((status) => (
                  <MenuItem key={status} value={status}>{formatStatus(status)}</MenuItem>
                ))}
              </TextField>
              {statusMutation.isError && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  {getApiErrorMessage(
                    statusMutation.error,
                    'Não foi possível atualizar o status do projeto.',
                  )}
                </Alert>
              )}
              <Button
                fullWidth
                sx={{ mt: 2 }}
                variant="contained"
                onClick={() => statusMutation.mutate(nextStatus)}
                disabled={statusMutation.isPending}
              >
                Atualizar status
              </Button>
            </ContentCard>

            <ContentCard title="Realocar membros">
              <Box>
                {employees.map((member) => {
                  const currentIds = selectedMembers.length
                    ? selectedMembers
                    : project.members.map((m) => m.id);
                  const checked = currentIds.includes(member.id);
                  return (
                    <FormControlLabel
                      key={member.id}
                      control={(
                        <Checkbox
                          checked={checked}
                          onChange={(event) => {
                            const base = selectedMembers.length
                              ? selectedMembers
                              : project.members.map((m) => m.id);
                            if (event.target.checked) {
                              setSelectedMembers([...base, member.id]);
                            } else {
                              setSelectedMembers(base.filter((value) => value !== member.id));
                            }
                          }}
                        />
                      )}
                      label={member.name}
                    />
                  );
                })}
              </Box>
              {membersMutation.isError && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  {getApiErrorMessage(
                    membersMutation.error,
                    'Não foi possível atualizar a equipe do projeto.',
                    { members: employees },
                  )}
                </Alert>
              )}
              <Button
                fullWidth
                sx={{ mt: 2 }}
                variant="outlined"
                onClick={() => membersMutation.mutate(
                  selectedMembers.length ? selectedMembers : project.members.map((m) => m.id),
                )}
                disabled={membersMutation.isPending}
              >
                Salvar alocação
              </Button>
            </ContentCard>
          </Box>
        )}
      </Box>
    </AppLayout>
  );
}
