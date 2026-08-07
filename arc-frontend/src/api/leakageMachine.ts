import { apiClient } from './client';

export interface LiveChamber {
  batchId: string;
  partNumber: string;
  serialNumber: string;
  currentPressure: number | null;
  unit: string;
  warningThreshold: number;
  alarmThreshold: number;
  status: string;
  cycleTimeSeconds: number;
  timestamp: string;
}

export interface TrendPoint {
  serialNumber: string;
  partNumber: string;
  pressureValue: number;
  passed: boolean;
  timestamp: string;
}

export interface QueueItem {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  status: string;
}

export interface TestedRecord {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  pressureValue: number;
  unit: string;
  status: string;
  timestamp: string;
}

export interface LeakageMachineState {
  machineStatus: string;
  activeBatch: string;
  fileName: string;
  warningThreshold: number;
  alarmThreshold: number;
  unit: string;
  totalEmbossed: number;
  totalTested: number;
  passedParts: number;
  failedParts: number;
  progressPercent: number;
  activeChamber?: LiveChamber;
  trendData?: TrendPoint[];
  queue?: QueueItem[];
  history?: TestedRecord[];
}

export async function getLeakageMachineState(): Promise<LeakageMachineState> {
  const response = await apiClient.get<LeakageMachineState>('/api/leakage-testing/machine/state');
  return response.data;
}

export async function startLeakageMachine(): Promise<LeakageMachineState> {
  const response = await apiClient.post<LeakageMachineState>('/api/leakage-testing/machine/start');
  return response.data;
}

export async function pauseLeakageMachine(): Promise<LeakageMachineState> {
  const response = await apiClient.post<LeakageMachineState>('/api/leakage-testing/machine/pause');
  return response.data;
}

export async function resetLeakageMachine(): Promise<LeakageMachineState> {
  const response = await apiClient.post<LeakageMachineState>('/api/leakage-testing/machine/reset');
  return response.data;
}
