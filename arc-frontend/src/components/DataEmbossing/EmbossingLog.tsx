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
    ? 'bg-[#EBFDF5] text-[#00B074]'
    : isPrinting
      ? 'bg-[#EEF2FF] text-[#5E40FF]'
      : 'bg-[#FEF3C7] text-[#D97706]';

  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold ${className}`}>
      {label}
    </span>
  );
}

function JobTable({
  jobs,
  emptyMessage,
}: {
  jobs: EmbossingJob[];
  emptyMessage: string;
}) {
  if (jobs.length === 0) {
    return (
      <tr>
        <td colSpan={4} className="px-4 py-6 text-center text-sm text-gray-400 font-medium">
          {emptyMessage}
        </td>
      </tr>
    );
  }

  return (
    <>
      {jobs.map((job) => (
        <tr
          key={job.id}
          className="bg-white hover:bg-gray-50/50 transition-colors border-b border-gray-100 last:border-b-0"
        >
          <td className="px-4 py-4 font-semibold text-gray-600">{job.partNumber}</td>
          <td className="px-4 py-4 text-gray-600 font-medium">{job.serialNumber}</td>
          <td className="px-4 py-4">
            <StatusBadge status={job.embossingStatus} />
          </td>
          <td className="px-4 py-4 text-gray-500 font-medium">
            {formatDateTime(job.createdTime)}
          </td>
        </tr>
      ))}
    </>
  );
}

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

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-gray-150 shadow-sm relative">
        <div className="flex items-center space-x-2 mb-6">
          <h2 className="text-xl font-bold text-gray-900 tracking-tight">Embossing Log</h2>
          <Info className="w-4 h-4 text-gray-400 cursor-pointer hover:text-gray-600" />
        </div>

        <div className="border border-amber-100 rounded-2xl overflow-hidden shadow-sm">
          <div className="bg-[#FFFDF5] border-b border-amber-100 px-4 py-3 flex items-center justify-between">
            <div className="flex items-center space-x-2 text-amber-700">
              <Clock className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-bold text-sm sm:text-base">Pending & Active Jobs</span>
            </div>
            <span className="text-xs sm:text-sm font-semibold text-amber-600/90">
              {pendingJobs.length} pending
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-white text-gray-400 font-semibold border-b border-gray-100">
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
                  <JobTable jobs={tableJobs} emptyMessage="No pending or active jobs" />
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-gray-150 shadow-sm">
        <div className="flex items-center space-x-2 mb-6">
          <CheckCircle2 className="w-5 h-5 text-[#00B074]" />
          <h2 className="text-xl font-bold text-gray-900 tracking-tight">Completed Jobs</h2>
          <span className="text-sm font-semibold text-gray-400">({completedCount})</span>
        </div>

        <div className="border border-green-100 rounded-2xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-white text-gray-400 font-semibold border-b border-gray-100">
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
                ) : completedJobs.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-4 py-6 text-center text-sm text-gray-400 font-medium">
                      No completed jobs yet
                    </td>
                  </tr>
                ) : (
                  completedJobs.map((job) => (
                    <tr
                      key={job.id}
                      className="bg-white hover:bg-gray-50/50 transition-colors border-b border-gray-100 last:border-b-0"
                    >
                      <td className="px-4 py-4 font-semibold text-gray-600">{job.partNumber}</td>
                      <td className="px-4 py-4 text-gray-600 font-medium">{job.serialNumber}</td>
                      <td className="px-4 py-4">
                        <StatusBadge status={job.embossingStatus} />
                      </td>
                      <td className="px-4 py-4 text-gray-500 font-medium">
                        {formatDateTime(job.embossingCompletedTime)}
                      </td>
                    </tr>
                  ))
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
