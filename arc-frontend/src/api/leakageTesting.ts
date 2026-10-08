import { useEffect } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { apiClient } from './client';
import { API_BASE_URL } from '../config/api';
import { LeakageTestRecord, LeakageTestingDashboardData } from '../types';

const FALLBACK_DASHBOARD: LeakageTestingDashboardData = {
  activeBatch: 'Batch_1',
  failedCount: 0,
  passedCount: 0,
  batchProgressPercent: 100,
  completedCount: 100,
  totalParts: 100,
  dateDisplay: '06 August, 2026',
  batchStatus: '100% completed',
  failures: [],
  passed: [],
};

export async function getLeakageTestingDashboard(): Promise<LeakageTestingDashboardData> {
  try {
    const { data } = await apiClient.get<LeakageTestingDashboardData>('/api/leakage-testing');
    return data;
  } catch (error) {
    console.warn('Backend Leakage API unreachable, using fallback local data:', error);
    return FALLBACK_DASHBOARD;
  }
}

export async function updateLeakageJobAction(id: number | string, action: string): Promise<LeakageTestRecord> {
  try {
    const { data } = await apiClient.patch<LeakageTestRecord>(`/api/leakage-testing/jobs/${id}/action`, { action });
    return data;
  } catch (error) {
    console.warn('Failed to update leakage job action on backend, returning mock:', error);
    return {
      id: typeof id === 'number' ? id : parseInt(id, 10),
      partNo: 'UNKNOWN',
      serialNo: 'UNKNOWN',
      status: 'Failed',
      testValue: 0,
      direction: 'down',
      timestamp: new Date().toISOString(),
      attempt: '1',
      action,
    };
  }
}

export function useLeakageDashboard(enabled = true) {
  return useQuery({
    queryKey: ['leakage', 'dashboard'],
    queryFn: getLeakageTestingDashboard,
    enabled,
  });
}

export function useUpdateLeakageAction() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, action }: { id: number | string; action: string }) =>
      updateLeakageJobAction(id, action),
    // Optimistic updates
    onMutate: async ({ id, action }) => {
      await queryClient.cancelQueries({ queryKey: ['leakage', 'dashboard'] });

      const previousDashboard = queryClient.getQueryData<LeakageTestingDashboardData>(['leakage', 'dashboard']);

      if (previousDashboard) {
        queryClient.setQueryData<LeakageTestingDashboardData>(['leakage', 'dashboard'], {
          ...previousDashboard,
          failures: previousDashboard.failures.map((f) =>
            f.id === id ? { ...f, action } : f
          ),
        });
      }

      return { previousDashboard };
    },
    onError: (err, variables, context) => {
      if (context?.previousDashboard) {
        queryClient.setQueryData(['leakage', 'dashboard'], context.previousDashboard);
      }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['leakage'] });
    },
  });
}

export function useLeakageProgressSocket(enabled = true) {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!enabled) return;

    const handleLocalUpdate = () => {
      void queryClient.invalidateQueries({ queryKey: ['leakage'] });
    };

    window.addEventListener('leakage-data-updated', handleLocalUpdate);

    let channel: BroadcastChannel | null = null;
    if ('BroadcastChannel' in window) {
      channel = new BroadcastChannel('arc-leakage');
      channel.onmessage = () => {
        handleLocalUpdate();
      };
    }

    const endpoint = `${API_BASE_URL.replace(/\/$/, '')}/ws`;
    const client = new Client({
      webSocketFactory: () => new SockJS(endpoint) as unknown as WebSocket,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
      onConnect: () => {
        client.subscribe('/topic/leakage-testing', () => {
          void queryClient.invalidateQueries({ queryKey: ['leakage'] });
        });
        client.subscribe('/topic/leakage-progress', () => {
          void queryClient.invalidateQueries({ queryKey: ['leakage'] });
        });
      },
    });

    client.activate();
    return () => {
      window.removeEventListener('leakage-data-updated', handleLocalUpdate);
      if (channel) channel.close();
      void client.deactivate();
    };
  }, [enabled, queryClient]);
}
