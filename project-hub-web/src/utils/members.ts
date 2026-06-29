import type { Member, MemberRole } from '../types';

export function filterProjectManagers(members: Member[]): Member[] {
  return members.filter((member) => member.role === 'manager');
}

export function filterProjectEmployees(members: Member[]): Member[] {
  return members.filter((member): member is Member & { role: 'employee' } =>
    member.role === 'employee');
}

export function isMemberRole(value: string): value is MemberRole {
  return value === 'manager'
    || value === 'employee'
    || value === 'intern'
    || value === 'consultant';
}
