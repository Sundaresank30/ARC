export type UserRole = 'manager' | 'operator';

export type EmbossingStatus = 'PENDING' | 'IN_MACHINE' | 'PRINTING' | 'COMPLETED';
export type MachineStatus = 'WAITING' | 'IN_MACHINE' | 'PRINTING' | 'IDLE';

export interface LoginRequest {
  role: string;
}

export interface LoginResponse {
  token: string;
  role: string;
  modules: string[];
}

export interface MeResponse {
  role: string;
  modules: string[];
}

export interface EmbossingJob {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  embossingStatus: EmbossingStatus;
  createdTime: string;
  embossingStartTime: string | null;
  embossingCompletedTime: string | null;
  machineStatus: MachineStatus;
  remarks: string | null;
}

export interface BatchProgress {
  batchId: string;
  totalRecords: number;
  completedRecords: number;
  pendingRecords: number;
  progressPercent: number;
  completed: boolean;
}

export interface EmbossingDashboard {
  activeBatch: string;
  pendingCount: number;
  batchProgress: BatchProgress[];
  jobs: EmbossingJob[];
}

export interface EmbossingProgress {
  jobId: number;
  batchId: string;
  jobStatus: EmbossingStatus;
  totalCount: number;
  completedCount: number;
  pendingCount: number;
  progressPercent: number;
  completed: boolean;
  job: EmbossingJob;
  batchProgress: BatchProgress;
}

export interface CurrentMachine {
  partNumber: string | null;
  serialNumber: string | null;
  machineStatus: MachineStatus;
}

export interface ApiError {
  message: string;
}
