import React from 'react';
import { Calendar, AlertCircle, Loader2 } from 'lucide-react';
import { ActiveBatchCard } from './ActiveBatchCard';
import { EmbossingLog } from './EmbossingLog';
import {
  useCompletedJobs,
  useEmbossingDashboard,
  useEmbossingProgressSocket,
  useEmbossingSimulationStarter,
} from '../../hooks/useEmbossing';

export const DataEmbossingPage: React.FC = () => {
  useEmbossingSimulationStarter(false);
  useEmbossingProgressSocket(true);

  const { data: dashboard, isLoading, isError, error } = useEmbossingDashboard(true);
  const { data: completedJobs = [] } = useCompletedJobs(true);

  const activeBatch = dashboard?.activeBatch;

  const pendingJobs =
    dashboard?.jobs.filter((job) => job.embossingStatus === 'PENDING') ?? [];

  const activeJobs =
    dashboard?.jobs.filter((job) =>
      ['IN_MACHINE', 'PRINTING'].includes(job.embossingStatus)
    ) ?? [];

  const currentBatchCompletedJobs = React.useMemo(() => {
    if (!activeBatch) {
      return completedJobs;
    }
    const filteredFromCompletedApi = completedJobs.filter((job) => job.batchId === activeBatch);
    if (filteredFromCompletedApi.length > 0) {
      return filteredFromCompletedApi;
    }
    return dashboard?.jobs.filter((job) => job.embossingStatus === 'COMPLETED' && (job.batchId === activeBatch || !job.batchId)) ?? [];
  }, [completedJobs, dashboard?.jobs, activeBatch]);

  const getFormattedDate = () => {
    return new Date().toLocaleDateString(undefined, {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  };

  return (
    <div className="animate-fade-in space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-semibold text-white tracking-tight leading-tight">
            Data Embossing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Monitor automated data embossing and production status
          </p>
        </div>

        <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm font-semibold text-gray-300">{getFormattedDate()}</span>
        </div>
      </div>

      {isError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Failed to load embossing data: {(error as Error)?.message ?? 'Unknown error'}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <ActiveBatchCard
            activeBatch={dashboard?.activeBatch ?? 'Batch_1'}
            progress={dashboard?.batchProgress?.[0]}
            isLoading={isLoading}
          />
        </div>

        <div className="lg:col-span-1">
          <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full hover:shadow-md transition-shadow duration-150">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-[15px] text-gray-300">Pending</span>
              <div className="w-8 h-8 rounded-lg bg-[#20150b] border border-[#f59e0b]/20 flex items-center justify-center text-[#f59e0b]">
                <AlertCircle className="w-4.5 h-4.5" />
              </div>
            </div>

            <div className="my-1">
              {isLoading ? (
                <Loader2 className="w-10 h-10 animate-spin text-[#f59e0b]" />
              ) : (
                <span className="text-[44px] font-bold leading-none tracking-tight text-white">
                  {dashboard?.pendingCount ?? 0}
                </span>
              )}
            </div>

            <div>
              <span className="text-xs font-semibold text-[#f59e0b]">-from this batch</span>
            </div>
          </div>
        </div>
      </div>

      <EmbossingLog
        pendingJobs={pendingJobs}
        activeJobs={activeJobs}
        completedJobs={currentBatchCompletedJobs}
        totalJobs={dashboard?.jobs.length ?? 0}
        batchProgress={dashboard?.batchProgress}
        isLoading={isLoading}
      />
    </div>
  );
};

export default DataEmbossingPage;
