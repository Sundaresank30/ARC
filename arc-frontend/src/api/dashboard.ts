import { apiClient } from './client';

export interface CarryForwardItem {
  id: number | string;
  partNo: string;
  serialNo: string;
  status: string;
  remainingSince?: string;
  batchId: string;
  action?: string;
}

export interface LeakageFailureItem {
  id: number | string;
  batchId?: string;
  partNo: string;
  serialNo: string;
  testValue: number;
  timestamp?: string;
  date?: string;
  status: string;
  direction?: string;
  attempt?: string;
  action?: string;
}

export interface DashboardSummaryResponse {
  completedCount: number;
  failedCount: number;
  totalBatches: number;
  carryForwardEmbossing: CarryForwardItem[];
  leakageTestingFailures: LeakageFailureItem[];
}

export async function getDashboardSummary(): Promise<DashboardSummaryResponse> {
  const response = await apiClient.get<DashboardSummaryResponse>('/api/dashboard');
  return response.data;
}

export async function resolveCarryForward(id: number | string): Promise<{ message: string }> {
  const response = await apiClient.post<{ message: string }>(`/api/dashboard/carry-forward/${id}/resolve`);
  return response.data;
}

export async function resolveLeakageFailure(id: number | string): Promise<{ message: string }> {
  const response = await apiClient.post<{ message: string }>(`/api/dashboard/leakage-failures/${id}/resolve`);
  return response.data;
}
