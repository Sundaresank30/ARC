import { apiClient } from './client';

export interface CreateBatchPayload {
  batchId: string;
  partNoSeries: string;
  partNoCount: number;
  serialNoSeries: string;
  serialNoCount: number;
}

export interface BatchItem {
  id: number;
  itemIndex: number;
  partNumber: string;
  serialNumber: string;
  status: string;
}

export interface ProductionBatchResponse {
  id: number;
  batchId: string;
  partNoSeries: string;
  partNoCount: number;
  serialNoSeries: string;
  serialNoCount: number;
  totalItems: number;
  createdAt: string;
  items?: BatchItem[];
}

export interface ServerDateResponse {
  formattedDate: string;
  isoDate: string;
}

export async function getServerDate(): Promise<ServerDateResponse> {
  const response = await apiClient.get<ServerDateResponse>('/api/data-preparation/server-date');
  return response.data;
}

export async function createProductionBatch(payload: CreateBatchPayload): Promise<ProductionBatchResponse> {
  const response = await apiClient.post<ProductionBatchResponse>('/api/data-preparation/batches', payload);
  return response.data;
}

export async function getAllProductionBatches(): Promise<ProductionBatchResponse[]> {
  const response = await apiClient.get<ProductionBatchResponse[]>('/api/data-preparation/batches');
  return response.data;
}

export async function getProductionBatchDetails(batchId: string): Promise<ProductionBatchResponse> {
  const response = await apiClient.get<ProductionBatchResponse>(`/api/data-preparation/batches/${batchId}`);
  return response.data;
}

export interface SourceDocumentResponse {
  id: number;
  batchId?: string;
  clientName?: string;
  plant?: string;
  product?: string;
  vacuumSetpoint?: string;
  maximumVacuum?: string;
  minimumVacuum?: string;
  warningThreshold?: string;
  alarmThreshold?: string;
  vacuumHoldTime?: string;
  motorCurrent?: string;
  motorTemperature?: string;
  operatingPressure?: string;
  cycleTime?: string;
  uploadedAt?: string;
}

export async function uploadSourceDocument(file: File, batchId?: string): Promise<SourceDocumentResponse> {
  const formData = new FormData();
  formData.append('file', file);
  if (batchId) {
    formData.append('batchId', batchId);
  }
  const response = await apiClient.post<SourceDocumentResponse>('/api/source-documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
}
