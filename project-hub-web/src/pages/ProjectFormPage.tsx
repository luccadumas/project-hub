import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Divider,
  FormControlLabel,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Controller, useForm } from 'react-hook-form';
import { useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';
import {
  createProject,
  fetchExternalMembers,
  fetchProject,
  updateProject,
} from '../api/projects';
import { AppLayout } from '../components/AppLayout';
import { ContentCard } from '../components/ContentCard';
import { CurrencyInput } from '../components/CurrencyInput';
import { DateInput } from '../components/DateInput';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { parseApiDate } from '../utils/date';
import { queryKeys } from '../api/queryKeys';
import { filterProjectEmployees, filterProjectManagers } from '../utils/members';

const isoDateField = (message: string) =>
  z.string().min(1, message).refine((value) => parseApiDate(value) !== null, 'Data invalida');

const schema = z.object({
  name: z.string().min(1, 'Nome obrigatorio'),
  startDate: isoDateField('Data de inicio obrigatoria'),
  expectedEndDate: isoDateField('Previsao de termino obrigatoria'),
  actualEndDate: z.string().optional().refine(
    (value) => !value || parseApiDate(value) !== null,
    'Data invalida',
  ),
  totalBudget: z.number().positive('Orcamento deve ser maior que zero'),
  description: z.string().optional(),
  managerId: z.number().positive('Gerente obrigatorio'),
  memberIds: z.array(z.number()).min(1, 'Selecione ao menos 1 membro').max(10, 'Maximo de 10 membros'),
});

type FormValues = z.infer<typeof schema>;

export function ProjectFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const projectQuery = useQuery({
    queryKey: queryKeys.project.detail(id ?? ''),
    queryFn: () => fetchProject(Number(id)),
    enabled: isEdit,
  });

  const membersQuery = useQuery({
    queryKey: queryKeys.externalMembers.all,
    queryFn: fetchExternalMembers,
  });

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: projectQuery.data
      ? {
          name: projectQuery.data.name,
          startDate: projectQuery.data.startDate,
          expectedEndDate: projectQuery.data.expectedEndDate,
          actualEndDate: projectQuery.data.actualEndDate,
          totalBudget: projectQuery.data.totalBudget,
          description: projectQuery.data.description,
          managerId: projectQuery.data.managerId,
          memberIds: projectQuery.data.members.map((member) => member.id),
        }
      : undefined,
    defaultValues: {
      name: '',
      startDate: '',
      expectedEndDate: '',
      totalBudget: 0,
      description: '',
      managerId: 0,
      memberIds: [],
    },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      isEdit ? updateProject(Number(id), values) : createProject(values),
    onSuccess: (project) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.projects.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.portfolioReport.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.project.detail(project.id) });
      navigate(`/projects/${project.id}`);
    },
  });

  if ((isEdit && projectQuery.isLoading) || membersQuery.isLoading) {
    return (
      <AppLayout>
        <LoadingState />
      </AppLayout>
    );
  }

  const managers = filterProjectManagers(membersQuery.data ?? []);
  const employees = filterProjectEmployees(membersQuery.data ?? []);

  return (
    <AppLayout>
      <PageHeader
        title={isEdit ? 'Editar projeto' : 'Novo projeto'}
        subtitle="Preencha os dados e selecione a equipe alocada"
      />

      {mutation.isError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Erro ao salvar projeto. Verifique os dados e regras de negocio.
        </Alert>
      )}

      <ContentCard>
        <Box component="form" onSubmit={handleSubmit((values: FormValues) => mutation.mutate(values))}>
          <Typography variant="subtitle1" sx={{ mb: 2 }}>Dados gerais</Typography>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { md: '2fr 1fr' } }}>
            <TextField fullWidth label="Nome" {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
            <Controller
              name="totalBudget"
              control={control}
              render={({ field }) => (
                <CurrencyInput
                  label="Orcamento total"
                  value={field.value}
                  onChange={field.onChange}
                  error={!!errors.totalBudget}
                  helperText={errors.totalBudget?.message}
                />
              )}
            />
            <Controller
              name="startDate"
              control={control}
              render={({ field }) => (
                <DateInput
                  label="Data de inicio"
                  value={field.value}
                  onChange={field.onChange}
                  error={!!errors.startDate}
                  helperText={errors.startDate?.message}
                />
              )}
            />
            <Controller
              name="expectedEndDate"
              control={control}
              render={({ field }) => (
                <DateInput
                  label="Previsao de termino"
                  value={field.value}
                  onChange={field.onChange}
                  error={!!errors.expectedEndDate}
                  helperText={errors.expectedEndDate?.message}
                />
              )}
            />
            {isEdit && (
              <Controller
                name="actualEndDate"
                control={control}
                render={({ field }) => (
                  <DateInput
                    label="Data real de termino"
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    error={!!errors.actualEndDate}
                    helperText={errors.actualEndDate?.message}
                  />
                )}
              />
            )}
            <TextField select fullWidth label="Gerente responsavel" {...register('managerId', { valueAsNumber: true })} error={!!errors.managerId} helperText={errors.managerId?.message}>
              {managers.map((member) => (
                <MenuItem key={member.id} value={member.id}>{member.name} ({member.role})</MenuItem>
              ))}
            </TextField>
          </Box>

          <TextField fullWidth multiline rows={3} label="Descricao" sx={{ mt: 2 }} {...register('description')} />

          <Divider sx={{ my: 3 }} />

          <Typography variant="subtitle1" sx={{ mb: 1 }}>Membros alocados (funcionarios)</Typography>
          <Controller
            name="memberIds"
            control={control}
            render={({ field }) => (
              <Box sx={{ display: 'grid', gridTemplateColumns: { sm: 'repeat(2, 1fr)' } }}>
                {employees.map((member) => (
                  <FormControlLabel
                    key={member.id}
                    control={(
                      <Checkbox
                        checked={field.value.includes(member.id)}
                        onChange={(event) => {
                          if (event.target.checked) {
                            field.onChange([...field.value, member.id]);
                          } else {
                            field.onChange(field.value.filter((value) => value !== member.id));
                          }
                        }}
                      />
                    )}
                    label={member.name}
                  />
                ))}
              </Box>
            )}
          />
          {errors.memberIds && (
            <Typography color="error" variant="caption">{errors.memberIds.message}</Typography>
          )}

          <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
            <Button type="submit" variant="contained" disabled={mutation.isPending}>
              {mutation.isPending ? 'Salvando...' : 'Salvar'}
            </Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancelar</Button>
          </Box>
        </Box>
      </ContentCard>
    </AppLayout>
  );
}
