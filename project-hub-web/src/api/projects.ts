import { api } from './client';
import type {
  Member,
  PageResponse,
  PortfolioReport,
  Project,
  ProjectFilters,
  ProjectFormData,
  ProjectStatus,
} from '../types';

export async function fetchProjects(filters: ProjectFilters = {}): Promise<PageResponse<Project>> {
  const { data } = await api.get<PageResponse<Project>>('/api/projects', { params: filters });
  return data;
}

export async function fetchProject(id: number): Promise<Project> {
  const { data } = await api.get<Project>(`/api/projects/${id}`);
  return data;
}

export async function createProject(payload: ProjectFormData): Promise<Project> {
  const { data } = await api.post<Project>('/api/projects', payload);
  return data;
}

export async function updateProject(id: number, payload: ProjectFormData): Promise<Project> {
  const { data } = await api.put<Project>(`/api/projects/${id}`, payload);
  return data;
}

export async function deleteProject(id: number): Promise<void> {
  await api.delete(`/api/projects/${id}`);
}

export async function updateProjectStatus(id: number, status: ProjectStatus): Promise<Project> {
  const { data } = await api.patch<Project>(`/api/projects/${id}/status`, { status });
  return data;
}

export async function allocateMembers(id: number, memberIds: number[]): Promise<Project> {
  const { data } = await api.put<Project>(`/api/projects/${id}/members`, { memberIds });
  return data;
}

export async function fetchPortfolioReport(): Promise<PortfolioReport> {
  const { data } = await api.get<PortfolioReport>('/api/reports/portfolio');
  return data;
}

export async function fetchExternalMembers(): Promise<Member[]> {
  const { data } = await api.get<Member[]>('/external/members');
  return data;
}
