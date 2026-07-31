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

export interface EmbossingDashboard {
  activeBatch: string;
  pendingCount: number;
  jobs: EmbossingJob[];
}

export interface CurrentMachine {
  partNumber: string | null;
  serialNumber: string | null;
  machineStatus: MachineStatus;
}

export interface ApiError {
  message: string;
}
