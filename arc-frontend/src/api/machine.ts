import { apiClient } from './client';

// ---------------------------------------------------------------------------
// Queue Engine API — backed by `embossing_queue` PostgreSQL table
// ---------------------------------------------------------------------------
export interface EmbossingQueueRecord {
  id: number;
  partNumber: string;
  serialNumber: string;
  /** WAITING | IN_PROGRESS | COMPLETED */
  status: string;
  printedAt: string | null;
  printedDate: string | null;
}

/** On load/restart — top 5 non-COMPLETED rows ordered by id asc. */
export async function getQueueBuffer(): Promise<EmbossingQueueRecord[]> {
  const { data } = await apiClient.get<EmbossingQueueRecord[]>('/api/machine/queue/buffer');
  return data;
}

/** Set status = IN_PROGRESS in the DB. */
export async function markQueueItemInProgress(id: number): Promise<EmbossingQueueRecord> {
  const { data } = await apiClient.put<EmbossingQueueRecord>(`/api/machine/queue/${id}/in-progress`);
  return data;
}

/** Set status = COMPLETED, stamped printed_at + printed_date in the DB. */
export async function markQueueItemCompleted(id: number): Promise<EmbossingQueueRecord> {
  const { data } = await apiClient.put<EmbossingQueueRecord>(`/api/machine/queue/${id}/complete`);
  return data;
}

/**
 * Fetch the next WAITING record for buffer refill.
 * Returns null when there are no more waiting items (server responds 204).
 */
export async function claimNextWaitingQueueItem(): Promise<EmbossingQueueRecord | null> {
  const response = await apiClient.put<EmbossingQueueRecord>('/api/machine/queue/claim-next', null, {
    validateStatus: (s) => s === 200 || s === 204,
  });
  return response.status === 204 ? null : response.data;
}

export async function fetchNextWaitingQueueItem(): Promise<EmbossingQueueRecord | null> {
  const response = await apiClient.get<EmbossingQueueRecord>('/api/machine/queue/next-waiting', {
    validateStatus: (s) => s === 200 || s === 204,
  });
  return response.status === 204 ? null : response.data;
}

/** Reset all rows to WAITING, clear timestamps. Returns fresh buffer. */
export async function resetQueue(): Promise<EmbossingQueueRecord[]> {
  const { data } = await apiClient.post<EmbossingQueueRecord[]>('/api/machine/queue/reset');
  return data;
}
