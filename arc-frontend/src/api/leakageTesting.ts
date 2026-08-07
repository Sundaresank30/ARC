import { apiClient } from './client';

export interface LeakageTestRecord {
  id?: number;
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Passed' | string;
  testValue: number;
  direction: 'up' | 'down' | string;
  timestamp: string;
  attempt: string;
  action: 'Scrap' | 'Pending' | 'Passed' | string;
}

export interface LeakageTestingDashboardData {
  activeBatch: string;
  failedCount: number;
  passedCount?: number;
  batchProgressPercent: number;
  completedCount: number;
  totalParts: number;
  dateDisplay: string;
  batchStatus?: string;
  failures: LeakageTestRecord[];
  passed?: LeakageTestRecord[];
}

export async function getLeakageTestingDashboard(): Promise<LeakageTestingDashboardData> {
  const response = await apiClient.get<LeakageTestingDashboardData>('/api/leakage-testing');
  return response.data;
}

export async function updateLeakageJobAction(id: number | string, action: string): Promise<LeakageTestRecord> {
  const response = await apiClient.patch<LeakageTestRecord>(`/api/leakage-testing/jobs/${id}/action`, { action });
  return response.data;
}
