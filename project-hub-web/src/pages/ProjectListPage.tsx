import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import SearchIcon from '@mui/icons-material/Search';
import {
  Alert,
  Box,
  Button,
  IconButton,
  InputAdornment,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { deleteProject, fetchProjects } from '../api/projects';
import { queryKeys } from '../api/queryKeys';
import { AppLayout } from '../components/AppLayout';
import { ContentCard } from '../components/ContentCard';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import type { ProjectStatus, RiskLevel } from '../types';
import {
  formatCurrency,
  formatStatus,
  PROJECT_STATUSES,
  RISK_LEVELS,
  RiskChip,
  StatusChip,
} from '../utils/formatters';

export function ProjectListPage() {
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [name, setName] = useState('');
  const [status, setStatus] = useState<ProjectStatus | ''>('');
  const [risk, setRisk] = useState<RiskLevel | ''>('');
  const debouncedName = useDebouncedValue(name);

  const projectsQuery = useQuery({
    queryKey: queryKeys.projects.list({ page, size, name: debouncedName, status, risk }),
    queryFn: () => fetchProjects({
      page,
      size,
      name: debouncedName || undefined,
      status: status || undefined,
      risk: risk || undefined,
      sort: 'name,asc',
    }),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteProject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.projects.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.portfolioReport.all });
    },
  });

  const handleDelete = (id: number, projectName: string) => {
    if (window.confirm(`Deseja excluir o projeto "${projectName}"?`)) {
      deleteMutation.mutate(id);
    }
  };

  return (
    <AppLayout>
      <PageHeader
        title="Projetos"
        subtitle="Gerencie o ciclo de vida e a alocação de equipes"
        action={
          isAdmin ? (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              component={RouterLink}
              to="/projects/new"
            >
              Novo projeto
            </Button>
          ) : undefined
        }
      />

      <ContentCard noPadding>
        <Box sx={{ p: 2, display: 'flex', gap: 2, flexWrap: 'wrap', borderBottom: '1px solid', borderColor: 'divider' }}>
          <TextField
            label="Buscar por nome"
            value={name}
            onChange={(e) => setName(e.target.value)}
            sx={{ minWidth: 240, flex: 1 }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" color="action" />
                  </InputAdornment>
                ),
              },
            }}
          />
          <TextField
            select
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value as ProjectStatus | '')}
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {PROJECT_STATUSES.map((item) => (
              <MenuItem key={item} value={item}>{formatStatus(item)}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Risco"
            value={risk}
            onChange={(e) => setRisk(e.target.value as RiskLevel | '')}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {RISK_LEVELS.map((item) => (
              <MenuItem key={item} value={item}>{item}</MenuItem>
            ))}
          </TextField>
        </Box>

        {deleteMutation.isError && (
          <Alert severity="error" sx={{ m: 2 }}>
            Não foi possível excluir o projeto. Verifique o status atual.
          </Alert>
        )}

        {projectsQuery.isLoading ? (
          <LoadingState />
        ) : projectsQuery.isError ? (
          <Alert severity="error" sx={{ m: 2 }}>Erro ao carregar projetos</Alert>
        ) : (
          <>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Projeto</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Risco</TableCell>
                  <TableCell>Gerente</TableCell>
                  <TableCell align="right">Orçamento</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {projectsQuery.data?.content.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                      <Typography color="text.secondary">Nenhum projeto encontrado</Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  projectsQuery.data?.content.map((project) => (
                    <TableRow key={project.id} hover>
                      <TableCell>
                        <Typography sx={{ fontWeight: 600 }}>{project.name}</Typography>
                      </TableCell>
                      <TableCell><StatusChip status={project.status} /></TableCell>
                      <TableCell><RiskChip risk={project.riskLevel} /></TableCell>
                      <TableCell>{project.managerName ?? project.managerId}</TableCell>
                      <TableCell align="right">{formatCurrency(project.totalBudget)}</TableCell>
                      <TableCell align="right">
                        <Tooltip title="Detalhes">
                          <IconButton
                            component={RouterLink}
                            to={`/projects/${project.id}`}
                            size="small"
                            aria-label={`Ver detalhes de ${project.name}`}
                          >
                            <VisibilityOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && (
                          <>
                            <Tooltip title="Editar">
                              <IconButton
                                component={RouterLink}
                                to={`/projects/${project.id}/edit`}
                                size="small"
                                aria-label={`Editar ${project.name}`}
                              >
                                <EditOutlinedIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Excluir">
                              <IconButton
                                onClick={() => handleDelete(project.id, project.name)}
                                size="small"
                                color="error"
                                aria-label={`Excluir ${project.name}`}
                              >
                                <DeleteOutlineOutlinedIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={projectsQuery.data?.totalElements ?? 0}
              page={page}
              onPageChange={(_, newPage) => setPage(newPage)}
              rowsPerPage={size}
              onRowsPerPageChange={(event) => {
                setSize(Number(event.target.value));
                setPage(0);
              }}
              labelRowsPerPage="Linhas por página"
            />
          </>
        )}
      </ContentCard>
    </AppLayout>
  );
}
