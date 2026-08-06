import React from 'react';
import { Info, Clock, Loader2, CheckCircle2 } from 'lucide-react';
import { BatchProgress, EmbossingJob } from '../../types';
import { formatDateTime, formatEmbossingStatus } from '../../utils/navigation';

interface EmbossingLogProps {
  pendingJobs: EmbossingJob[];
  activeJobs: EmbossingJob[];
  completedJobs: EmbossingJob[];
  totalJobs: number;
  batchProgress?: BatchProgress[];
  isLoading?: boolean;
}

function StatusBadge({ status }: { status: string }) {
  const label = formatEmbossingStatus(status);
  const isCompleted = status === 'COMPLETED';
  const isPrinting = status === 'PRINTING' || status === 'IN_MACHINE';

  const className = isCompleted
    ? 'bg-[#00B074]/10 text-[#00B074]'
    : isPrinting
      ? 'bg-[#5E40FF]/10 text-[#5E40FF]'
      : 'bg-[#D97706]/10 text-[#D97706]';

  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold ${className}`}>
      {label}
    </span>
  );
}

interface BatchProgressBarProps {
  percent: number;
  completedCount: number;
  totalCount: number;
  isActive: boolean;
}

const BatchProgressBar: React.FC<BatchProgressBarProps> = ({
  percent,
  completedCount,
  totalCount,
  isActive,
}) => {
  const isInProgress = percent > 0 && percent < 100;

  return (
    <div
      className={`relative w-full h-9 bg-[#041a13] rounded-lg overflow-hidden border border-[#00B074]/20 flex items-center transition-all duration-300 ${(isActive || isInProgress) ? 'animate-progress-active' : ''
        }`}
    >
      <div
        className="absolute top-0 left-0 h-full bg-[#00B074]/60 transition-[width] duration-500 ease-out"
        style={{ width: `${percent}%` }}
      />
      <div className="absolute inset-0 flex items-center justify-between px-4 z-10">
        <span className="text-xs sm:text-sm font-semibold text-emerald-400">
          Batch Progress: {percent}% Complete ({completedCount} / {totalCount} Parts)
        </span>
        <div className="flex items-center space-x-2 text-emerald-400">
          {(isActive || isInProgress) && (
            <Loader2 className="w-3.5 h-3.5 animate-spin" />
          )}
          <span className="text-xs sm:text-sm font-bold">
            {percent}%
          </span>
        </div>
      </div>
    </div>
  );
};

export const EmbossingLog: React.FC<EmbossingLogProps> = ({
  pendingJobs,
  activeJobs,
  completedJobs,
  totalJobs,
  batchProgress,
  isLoading = false,
}) => {
  const completedCount = completedJobs.length;
  const tableJobs = [...activeJobs, ...pendingJobs];
  const totalCount = tableJobs.length + completedCount;
  const percent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;
  const isActive = activeJobs.length > 0;

  return (
    <div className="bg-[#0D0E19] rounded-3xl p-6 sm:p-8 border border-[#1b172a] shadow-sm relative">
      <div className="flex items-center space-x-2 mb-6">
        <h2 className="text-xl font-semibold text-white tracking-tight">Embossing Log</h2>
        <Info className="w-4 h-4 text-gray-400 cursor-pointer hover:text-gray-600" />
      </div>

      <div className="space-y-6">
        {/* Panel 1: Pending & Active Jobs */}
        <div className="border border-[#f59e0b]/20 rounded-2xl overflow-hidden shadow-sm">
          <div className="bg-[#20150b]/50 border-b border-[#f59e0b]/20 px-4 py-3 flex items-center justify-between">
            <div className="flex items-center space-x-2 text-[#f59e0b]">
              <Clock className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-semibold text-sm sm:text-base">Pending & Active Jobs</span>
            </div>
            <span className="text-xs sm:text-sm font-semibold text-[#f59e0b]/90">
              {pendingJobs.length} pending
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-[#13111c]/30 text-gray-500 font-semibold border-b border-[#221e33]">
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Created</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center">
                      <Loader2 className="w-6 h-6 animate-spin text-gray-400 mx-auto" />
                    </td>
                  </tr>
                ) : (
                  <>
                    {/* Batch Progress Bar Row */}
                    <tr>
                      <td colSpan={4} className="px-4 py-3">
                        <BatchProgressBar
                          percent={percent}
                          completedCount={completedCount}
                          totalCount={totalCount}
                          isActive={isActive}
                        />
                      </td>
                    </tr>

                    {tableJobs.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-4 py-6 text-center text-sm text-gray-400 font-medium">
                          No pending or active jobs
                        </td>
                      </tr>
                    ) : (
                      tableJobs.map((job) => (
                        <tr
                          key={job.id}
                          className="bg-[#0D0E19] hover:bg-[#151221]/30 transition-colors border-b border-[#1b172a] last:border-b-0"
                        >
                          <td className="px-4 py-4 text-gray-400 font-medium">{job.partNumber}</td>
                          <td className="px-4 py-4 text-gray-400 font-medium">{job.serialNumber}</td>
                          <td className="px-4 py-4">
                            <StatusBadge status={job.embossingStatus} />
                          </td>
                          <td className="px-4 py-4 text-gray-500 font-medium">
                            {formatDateTime(job.createdTime)}
                          </td>
                        </tr>
                      ))
                    )}
                  </>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Panel 2: Completed Jobs */}
        <div className="border border-[#00B074]/30 rounded-2xl overflow-hidden shadow-sm">
          <div className="bg-[#0a231b]/50 border-b border-[#00B074]/30 px-4 py-3 flex items-center justify-between">
            <div className="flex items-center space-x-2 text-[#00B074]">
              <CheckCircle2 className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-semibold text-sm sm:text-base">Completed Jobs</span>
            </div>
            <span className="text-xs sm:text-sm font-semibold text-[#00B074]">
              {completedCount} completed
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-[#13111c]/30 text-gray-500 font-semibold border-b border-[#221e33]">
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Completed</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center">
                      <Loader2 className="w-6 h-6 animate-spin text-gray-400 mx-auto" />
                    </td>
                  </tr>
                ) : (
                  <>
                    {/* Batch Progress Bar Row */}
                    <tr>
                      <td colSpan={4} className="px-4 py-3">
                        <BatchProgressBar
                          percent={percent}
                          completedCount={completedCount}
                          totalCount={totalCount}
                          isActive={isActive}
                        />
                      </td>
                    </tr>

                    {completedJobs.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-4 py-6 text-center text-sm text-gray-400 font-medium">
                          No completed jobs yet
                        </td>
                      </tr>
                    ) : (
                      completedJobs.map((job) => (
                        <tr
                          key={job.id}
                          className="bg-[#0D0E19] hover:bg-[#151221]/30 transition-colors border-b border-[#1b172a] last:border-b-0"
                        >
                          <td className="px-4 py-4 text-gray-400 font-medium">{job.partNumber}</td>
                          <td className="px-4 py-4 text-gray-400 font-medium">{job.serialNumber}</td>
                          <td className="px-4 py-4">
                            <StatusBadge status={job.embossingStatus} />
                          </td>
                          <td className="px-4 py-4 text-gray-500 font-medium">
                            {formatDateTime(job.embossingCompletedTime)}
                          </td>
                        </tr>
                      ))
                    )}
                  </>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmbossingLog;
