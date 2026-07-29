import { useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import * as embossingApi from '../api/embossing';

const DASHBOARD_POLL_INTERVAL = 2000;

export function useEmbossingDashboard(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'dashboard'],
    queryFn: embossingApi.getDashboard,
    enabled,
    refetchInterval: DASHBOARD_POLL_INTERVAL,
  });
}

export function useCompletedJobs(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'completed'],
    queryFn: embossingApi.getCompletedJobs,
    enabled,
    refetchInterval: DASHBOARD_POLL_INTERVAL,
  });
}

export function useCurrentMachine(enabled = true) {
  return useQuery({
    queryKey: ['embossing', 'current-machine'],
    queryFn: embossingApi.getCurrentMachine,
    enabled,
    refetchInterval: 1000,
  });
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
