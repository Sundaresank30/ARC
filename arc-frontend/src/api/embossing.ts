import { apiClient } from './client';
import {
  CurrentMachine,
  EmbossingDashboard,
  EmbossingJob,
} from '../types';

export async function getDashboard(): Promise<EmbossingDashboard> {
  const { data } = await apiClient.get<EmbossingDashboard>('/api/embossing/dashboard');
  return data;
}

export async function getPendingJobs(): Promise<EmbossingJob[]> {
  const { data } = await apiClient.get<EmbossingJob[]>('/api/embossing/pending');
  return data;
}

export async function getCompletedJobs(): Promise<EmbossingJob[]> {
  const { data } = await apiClient.get<EmbossingJob[]>('/api/embossing/completed');
  return data;
}

export async function getCurrentMachine(): Promise<CurrentMachine> {
  const { data } = await apiClient.get<CurrentMachine>('/api/embossing/current-machine');
  return data;
}

export async function startSimulation(): Promise<{ message: string; simulationRunning: boolean }> {
  const { data } = await apiClient.post<{ message: string; simulationRunning: boolean }>(
    '/api/embossing/start'
  );
  return data;
}
