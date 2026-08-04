import { useEffect, useRef } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import * as embossingApi from '../api/embossing';
import { API_BASE_URL } from '../config/api';
import { EmbossingDashboard, EmbossingJob, EmbossingProgress } from '../types';

export function useEmbossingDashboard(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'dashboard'],
    queryFn: embossingApi.getDashboard,
    enabled,
  });
}

export function useCompletedJobs(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'completed'],
    queryFn: embossingApi.getCompletedJobs,
    enabled,
  });
}

export function useCurrentMachine(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'current-machine'],
    queryFn: embossingApi.getCurrentMachine,
    enabled,
  });
}

/** Keeps the existing REST load for first paint, then applies committed server events in-place. */
export function useEmbossingProgressSocket(enabled = true) {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!enabled) return;

    const handleLocalUpdate = () => {
      void queryClient.invalidateQueries({ queryKey: ['embossing'] });
    };

    window.addEventListener('embossing-data-updated', handleLocalUpdate);

    let channel: BroadcastChannel | null = null;
    if ('BroadcastChannel' in window) {
      channel = new BroadcastChannel('arc-embossing');
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
        client.subscribe('/topic/embossing-progress', (message) => {
          let progress: EmbossingProgress;
          try {
            progress = JSON.parse(message.body) as EmbossingProgress;
          } catch {
            return;
          }

          queryClient.setQueryData<EmbossingDashboard>(['embossing', 'dashboard'], (dashboard) => {
            if (!dashboard) {
              return {
                activeBatch: progress.batchId,
                pendingCount: progress.pendingCount,
                batchProgress: progress.batchProgress ? [progress.batchProgress] : [],
                jobs: progress.job ? [progress.job] : [],
              };
            }

            const existingJobIndex = dashboard.jobs.findIndex((j) => j.id === progress.jobId);
            let updatedJobs = [...dashboard.jobs];
            if (existingJobIndex >= 0 && progress.job) {
              updatedJobs[existingJobIndex] = progress.job;
            } else if (progress.job) {
              updatedJobs.push(progress.job);
            }

            const updatedBatchProgress = dashboard.batchProgress.some(b => b.batchId === progress.batchId)
              ? dashboard.batchProgress.map((batch) =>
                  batch.batchId === progress.batchId ? progress.batchProgress : batch
                )
              : (progress.batchProgress ? [...dashboard.batchProgress, progress.batchProgress] : dashboard.batchProgress);

            return {
              ...dashboard,
              activeBatch: progress.batchId || dashboard.activeBatch,
              pendingCount: progress.pendingCount,
              batchProgress: updatedBatchProgress,
              jobs: updatedJobs,
            };
          });

          queryClient.setQueryData<EmbossingJob[]>(['embossing', 'completed'], (jobs = []) => {
            const withoutUpdatedJob = jobs.filter((job) => job.id !== progress.jobId);
            return progress.jobStatus === 'COMPLETED' && progress.job
              ? [...withoutUpdatedJob, progress.job]
              : withoutUpdatedJob;
          });

          void queryClient.invalidateQueries({ queryKey: ['embossing'] });
        });
      },
    });

    client.activate();
    return () => {
      window.removeEventListener('embossing-data-updated', handleLocalUpdate);
      if (channel) channel.close();
      void client.deactivate();
    };
  }, [enabled, queryClient]);
}

export function useEmbossingSimulationStarter(enabled = true) {
  const startedRef = useRef(false);

  useEffect(() => {
    if (!enabled || startedRef.current) {
      return;
    }

    startedRef.current = true;

    embossingApi.startSimulation().catch((error: unknown) => {
      const status = (error as { response?: { status?: number } })?.response?.status;

      if (status !== 409) {
        console.error('Failed to start embossing simulation:', error);
      }
    });
  }, [enabled]);
}
