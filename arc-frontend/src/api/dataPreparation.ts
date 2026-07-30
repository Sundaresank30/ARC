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

export async function createProductionBatch(payload: CreateBatchPayload): Promise<ProductionBatchResponse> {
  const response = await apiClient.post<ProductionBatchResponse>('/data-preparation/batches', payload);
  return response.data;
}

export async function getAllProductionBatches(): Promise<ProductionBatchResponse[]> {
  const response = await apiClient.get<ProductionBatchResponse[]>('/data-preparation/batches');
  return response.data;
}

export async function getProductionBatchDetails(batchId: string): Promise<ProductionBatchResponse> {
  const response = await apiClient.get<ProductionBatchResponse>(`/data-preparation/batches/${batchId}`);
  return response.data;
}
