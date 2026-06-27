export const queryKeys = {
  projects: {
    all: ['projects'] as const,
    list: (filters: Record<string, unknown>) => ['projects', filters] as const,
  },
  project: {
    detail: (id: number | string) => ['project', String(id)] as const,
  },
  portfolioReport: {
    all: ['portfolio-report'] as const,
  },
  externalMembers: {
    all: ['external-members'] as const,
  },
};
