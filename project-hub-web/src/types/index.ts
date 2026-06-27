export type MemberRole = 'gerente' | 'funcionario' | 'estagiario' | 'consultor';

export type ProjectStatus =
  | 'EM_ANALISE'
  | 'ANALISE_REALIZADA'
  | 'ANALISE_APROVADA'
  | 'INICIADO'
  | 'PLANEJADO'
  | 'EM_ANDAMENTO'
  | 'ENCERRADO'
  | 'CANCELADO';

export type RiskLevel = 'BAIXO' | 'MEDIO' | 'ALTO';

export interface MemberSummary {
  id: number;
  name: string;
  role: MemberRole;
}

export interface Project {
  id: number;
  name: string;
  startDate: string;
  expectedEndDate: string;
  actualEndDate?: string;
  totalBudget: number;
  description?: string;
  managerId: number;
  managerName?: string;
  status: ProjectStatus;
  riskLevel: RiskLevel;
  members: MemberSummary[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface PortfolioReport {
  projectsCountByStatus: Record<ProjectStatus, number>;
  totalBudgetByStatus: Record<ProjectStatus, number>;
  averageClosedProjectDurationDays: number;
  uniqueAllocatedMembers: number;
}

export interface Member {
  id: number;
  name: string;
  role: MemberRole;
}

export interface ProjectFilters {
  page?: number;
  size?: number;
  name?: string;
  status?: ProjectStatus;
  risk?: RiskLevel;
  managerId?: number;
  startDateFrom?: string;
  startDateTo?: string;
  expectedEndDateFrom?: string;
  expectedEndDateTo?: string;
  sort?: string;
}

export interface ProjectFormData {
  name: string;
  startDate: string;
  expectedEndDate: string;
  actualEndDate?: string;
  totalBudget: number;
  description?: string;
  managerId: number;
  memberIds: number[];
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}
