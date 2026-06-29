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
import { useEffect } from 'react';
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
import { parseApiDate, isIsoDateBefore } from '../utils/date';
import { formatMemberLabel } from '../utils/formatters';
import { queryKeys } from '../api/queryKeys';
import { filterProjectEmployees, filterProjectManagers } from '../utils/members';
import { getApiErrorMessage } from '../utils/errors';

const isoDateField = (message: string) =>
  z.string().min(1, message).refine((value) => parseApiDate(value) !== null, 'Data inválida');

const schema = z.object({
  name: z.string().min(1, 'Nome obrigatório'),
  startDate: isoDateField('Data de início obrigatória'),
  expectedEndDate: isoDateField('Previsão de término obrigatória'),
  actualEndDate: z.string().optional().refine(
    (value) => !value || parseApiDate(value) !== null,
    'Data inválida',
  ),
  totalBudget: z.number().positive('Orçamento deve ser maior que zero'),
  description: z.string().optional(),
  managerId: z.number().positive('Gerente obrigatório'),
  memberIds: z.array(z.number()).min(1, 'Selecione ao menos 1 membro').max(10, 'Máximo de 10 membros'),
}).superRefine((data, ctx) => {
  if (isIsoDateBefore(data.expectedEndDate, data.startDate)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Previsão de término não pode ser anterior à data de início',
      path: ['expectedEndDate'],
    });
  }

  if (data.actualEndDate && isIsoDateBefore(data.actualEndDate, data.startDate)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Data real de término não pode ser anterior à data de início',
      path: ['actualEndDate'],
    });
  }
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
    reset,
    watch,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
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

  useEffect(() => {
    if (!projectQuery.data) {
      return;
    }

    reset({
      name: projectQuery.data.name,
      startDate: projectQuery.data.startDate,
      expectedEndDate: projectQuery.data.expectedEndDate,
      actualEndDate: projectQuery.data.actualEndDate,
      totalBudget: projectQuery.data.totalBudget,
      description: projectQuery.data.description ?? '',
      managerId: projectQuery.data.managerId,
      memberIds: projectQuery.data.members.map((member) => member.id),
    });
  }, [projectQuery.data, reset]);

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
  const startDate = watch('startDate');

  return (
    <AppLayout>
      <PageHeader
        title={isEdit ? 'Editar projeto' : 'Novo projeto'}
        subtitle="Preencha os dados e selecione a equipe alocada"
      />

      {mutation.isError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {getApiErrorMessage(
            mutation.error,
            'Não foi possível salvar o projeto. Revise os dados informados e tente novamente.',
            { members: membersQuery.data },
          )}
        </Alert>
      )}

      <ContentCard>
        <Box component="form" onSubmit={handleSubmit((values: FormValues) => mutation.mutate(values))}>
          <Typography variant="subtitle1" sx={{ mb: 2 }}>Dados gerais</Typography>
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '2fr 1fr' } }}>
            <TextField fullWidth label="Nome" {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
            <Controller
              name="totalBudget"
              control={control}
              render={({ field }) => (
                <CurrencyInput
                  label="Orçamento total"
                  value={field.value}
                  onChange={field.onChange}
                  error={!!errors.totalBudget}
                  helperText={errors.totalBudget?.message}
                />
              )}
            />
          </Box>

          <Box
            sx={{
              display: 'grid',
              gap: 2,
              mt: 2,
              alignItems: 'start',
              gridTemplateColumns: {
                xs: '1fr',
                md: isEdit
                  ? 'minmax(140px, 1fr) minmax(140px, 1fr) minmax(140px, 1fr) minmax(200px, 2fr)'
                  : 'minmax(140px, 1fr) minmax(140px, 1fr) minmax(200px, 2fr)',
              },
            }}
          >
            <Controller
              name="startDate"
              control={control}
              render={({ field }) => (
                <DateInput
                  label="Data de início"
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
                  label="Previsão de término"
                  value={field.value}
                  onChange={field.onChange}
                  minDate={startDate}
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
                    label="Data real de término"
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    minDate={startDate}
                    error={!!errors.actualEndDate}
                    helperText={errors.actualEndDate?.message}
                  />
                )}
              />
            )}
            <Controller
              name="managerId"
              control={control}
              render={({ field }) => (
                <TextField
                  select
                  fullWidth
                  label="Gerente responsável"
                  value={field.value || ''}
                  onChange={(event) => field.onChange(Number(event.target.value))}
                  onBlur={field.onBlur}
                  error={!!errors.managerId}
                  helperText={errors.managerId?.message ?? '\u00a0'}
                  slotProps={{ formHelperText: { sx: { minHeight: '1.25em' } } }}
                >
                  {managers.map((member) => (
                    <MenuItem key={member.id} value={member.id}>
                      {formatMemberLabel(member.name, member.role)}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Box>

          <TextField fullWidth multiline rows={3} label="Descrição" sx={{ mt: 2 }} {...register('description')} />

          <Divider sx={{ my: 3 }} />

          <Typography variant="subtitle1" sx={{ mb: 1 }}>Membros alocados (funcionários)</Typography>
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
