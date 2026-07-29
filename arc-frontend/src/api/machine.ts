import { apiClient } from './client';

export interface MachineRecord {
  id: number;
  serialNumber: string;
  partNumber: string;
  status: 'waiting' | 'completed' | string;
}

export async function getMachineRecords(): Promise<MachineRecord[]> {
  const response = await apiClient.get<MachineRecord[]>('/api/machine/records');
  return response.data;
}

export async function updateMachineRecordStatus(
  id: number,
  status: 'waiting' | 'completed' | string
): Promise<MachineRecord> {
  const response = await apiClient.put<MachineRecord>(`/api/machine/records/${id}/status`, {
    status,
  });
  return response.data;
}

export async function resetMachineRecords(): Promise<MachineRecord[]> {
  const response = await apiClient.post<MachineRecord[]>('/api/machine/records/reset');
  return response.data;
}
