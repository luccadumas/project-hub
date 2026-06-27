import type { Member, MemberRole } from '../types';

export function filterProjectManagers(members: Member[]): Member[] {
  return members.filter((member) => member.role === 'gerente');
}

export function filterProjectEmployees(members: Member[]): Member[] {
  return members.filter((member): member is Member & { role: 'funcionario' } =>
    member.role === 'funcionario');
}

export function isMemberRole(value: string): value is MemberRole {
  return value === 'gerente'
    || value === 'funcionario'
    || value === 'estagiario'
    || value === 'consultor';
}
