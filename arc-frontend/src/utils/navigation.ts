import { UserRole } from '../types';

export const MODULE_TO_TAB: Record<string, string> = {
  Dashboard: 'dashboard',
  'Data Preparation': 'data-preparation',
  'Data Embossing': 'data-embossing',
  'Leakage Machine': 'leakage-machine',
  'Leakage Testing': 'leakage-testing',
  Machine: 'machine',
  Settings: 'settings',
};

export function modulesToTabs(modules: string[]): string[] {
  return modules
    .map((module) => MODULE_TO_TAB[module])
    .filter((tab): tab is string => Boolean(tab));
}

export function roleToApiRole(role: UserRole): string {
  return role === 'manager' ? 'MANAGER' : 'OPERATOR';
}

export function apiRoleToUserRole(role: string): UserRole {
  return role.toUpperCase() === 'MANAGER' ? 'manager' : 'operator';
}

export function getDefaultTab(role: UserRole): string {
  return role === 'operator' ? 'machine' : 'dashboard';
}

export function formatEmbossingStatus(status: string): string {
  const labels: Record<string, string> = {
    PENDING: 'Pending',
    IN_MACHINE: 'In Machine',
    PRINTING: 'Printing',
    COMPLETED: 'Completed',
  };

  return labels[status] ?? status;
}

export function formatMachineStatus(status: string): string {
  const labels: Record<string, string> = {
    WAITING: 'Waiting',
    IN_MACHINE: 'In Machine',
    PRINTING: 'Printing',
    IDLE: 'Idle',
  };

  return labels[status] ?? status;
}

export function formatDateTime(value: string | null): string {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  return date.toLocaleString(undefined, {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: 'short',
  });
}
