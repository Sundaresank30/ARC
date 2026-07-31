import React from 'react';
import { Calendar, AlertCircle, Loader2 } from 'lucide-react';
import { ActiveBatchCard } from './ActiveBatchCard';
import { EmbossingLog } from './EmbossingLog';
import {
  useCompletedJobs,
  useEmbossingDashboard,
  useEmbossingSimulationStarter,
} from '../../hooks/useEmbossing';

export const DataEmbossingPage: React.FC = () => {
  useEmbossingSimulationStarter(true);

  const { data: dashboard, isLoading, isError, error } = useEmbossingDashboard(true);
  const { data: completedJobs = [] } = useCompletedJobs(true);

  const pendingJobs =
    dashboard?.jobs.filter((job) => job.embossingStatus === 'PENDING') ?? [];

  const activeJobs =
    dashboard?.jobs.filter((job) =>
      ['IN_MACHINE', 'PRINTING'].includes(job.embossingStatus)
    ) ?? [];

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
          <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight">
            Data Embossing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
            Monitor automated data embossing and production status
          </p>
        </div>

        <div className="flex items-center space-x-2 bg-white border border-gray-150 px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-semibold text-gray-700">{getFormattedDate()}</span>
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
            isLoading={isLoading}
          />
        </div>

        <div className="lg:col-span-1">
          <div className="bg-white border border-gray-150 rounded-2xl p-6 shadow-sm flex flex-col justify-between min-h-[135px] hover:shadow-md transition-shadow duration-150">
            <div className="flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-[#D97706]" />
              <span className="font-bold text-[15px] text-[#D97706]">Pending</span>
            </div>

            <div className="my-1">
              {isLoading ? (
                <Loader2 className="w-10 h-10 animate-spin text-[#D97706]" />
              ) : (
                <span className="text-[44px] font-bold leading-none tracking-tight text-[#D97706]">
                  {dashboard?.pendingCount ?? 0}
                </span>
              )}
            </div>

            <div>
              <span className="text-xs font-semibold text-[#D97706]/85">-from this batch</span>
            </div>
          </div>
        </div>
      </div>

      <EmbossingLog
        pendingJobs={pendingJobs}
        activeJobs={activeJobs}
        completedJobs={completedJobs}
        totalJobs={dashboard?.jobs.length ?? 0}
        isLoading={isLoading}
      />
    </div>
  );
};

export default DataEmbossingPage;
