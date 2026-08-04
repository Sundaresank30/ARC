import { apiClient } from './client';
import {
  CurrentMachine,
  EmbossingDashboard,
  EmbossingJob,
} from '../types';

const FALLBACK_DASHBOARD: EmbossingDashboard = {
  activeBatch: '',
  pendingCount: 0,
  batchProgress: [],
  jobs: [],
};

const FALLBACK_COMPLETED_JOBS: EmbossingJob[] = [];

const FALLBACK_MACHINE: CurrentMachine = {
  partNumber: null,
  serialNumber: null,
  machineStatus: 'IDLE',
};

export async function getDashboard(): Promise<EmbossingDashboard> {
  try {
    const { data } = await apiClient.get<EmbossingDashboard>('/api/embossing/dashboard');
    return data;
  } catch (error) {
    console.warn('Backend Embossing API unreachable, using fallback local data:', error);
    return FALLBACK_DASHBOARD;
  }
}

export async function getPendingJobs(): Promise<EmbossingJob[]> {
  try {
    const { data } = await apiClient.get<EmbossingJob[]>('/api/embossing/pending');
    return data;
  } catch (error) {
    console.warn('Backend Embossing API unreachable, using fallback local data:', error);
    return FALLBACK_DASHBOARD.jobs.filter((j) => j.embossingStatus === 'PENDING');
  }
}

export async function getCompletedJobs(): Promise<EmbossingJob[]> {
  try {
    const { data } = await apiClient.get<EmbossingJob[]>('/api/embossing/completed');
    return data;
  } catch (error) {
    console.warn('Backend Embossing API unreachable, using fallback local data:', error);
    return FALLBACK_COMPLETED_JOBS;
  }
}

export async function getCurrentMachine(): Promise<CurrentMachine> {
  try {
    const { data } = await apiClient.get<CurrentMachine>('/api/embossing/current-machine');
    return data;
  } catch (error) {
    console.warn('Backend Embossing API unreachable, using fallback local data:', error);
    return FALLBACK_MACHINE;
  }
}

export async function startSimulation(): Promise<{ message: string; simulationRunning: boolean }> {
  try {
    const { data } = await apiClient.post<{ message: string; simulationRunning: boolean }>(
      '/api/embossing/start'
    );
    return data;
  } catch (error) {
    console.warn('Backend Embossing Simulation API unreachable, skipping:', error);
    return { message: 'Local fallback mode active', simulationRunning: false };
  }
}
