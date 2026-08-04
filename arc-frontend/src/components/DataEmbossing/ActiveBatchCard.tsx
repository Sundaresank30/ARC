import React from 'react';
import { FileText, Database, Loader2, CheckCircle2 } from 'lucide-react';
import { BatchProgress } from '../../types';

interface ActiveBatchCardProps {
  activeBatch: string;
  progress?: BatchProgress;
  isLoading?: boolean;
}

export const ActiveBatchCard: React.FC<ActiveBatchCardProps> = ({
  activeBatch,
  progress,
  isLoading = false,
}) => {
  return (
    <div className="bg-white border border-gray-150 rounded-2xl p-6 shadow-sm flex flex-col min-h-[135px]">
      <div className="flex items-center justify-between mb-5">
        <h3 className="font-bold text-sm text-gray-800 uppercase tracking-wider">
          Active Batch
        </h3>
        {progress && (
          <span className="text-sm font-semibold text-[#00B074]">
            {progress.progressPercent}% Complete
          </span>
        )}
      </div>

      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <div className="w-9 h-9 rounded-lg bg-[#EBFDF5] flex items-center justify-center text-[#00B074]">
            <FileText className="w-5 h-5 stroke-[2]" />
          </div>
          {isLoading ? (
            <Loader2 className="w-5 h-5 animate-spin text-[#00B074]" />
          ) : (
            <span className="text-base font-bold text-[#00B074]">{activeBatch}</span>
          )}
        </div>

        <div className="flex-1 flex items-center max-w-[180px]">
          <div className="w-1.5 h-1.5 rounded-full bg-gray-300 shrink-0" />
          <div className="flex-1 h-[1.5px] bg-gray-300" />
          <div className="w-2 h-2 border-t-[1.5px] border-r-[1.5px] border-gray-300 transform rotate-45 -ml-1 shrink-0" />
        </div>

        <div className="flex items-center space-x-2">
          <div className="w-9 h-9 rounded-lg bg-gray-50 flex items-center justify-center text-gray-400">
            <Database className="w-5 h-5 stroke-[2]" />
          </div>
          <span className="text-base font-bold text-gray-400">Data Embossing</span>
        </div>
      </div>

      {progress && (
        <div className="mt-5 rounded-xl border border-gray-100 bg-gray-50 p-4 space-y-2">
          <div className="flex items-center justify-between text-sm text-gray-600">
            <span>Completed</span>
            <span className="font-semibold text-gray-900">
              {progress.completedRecords}/{progress.totalRecords}
            </span>
          </div>
          <div className="h-2 rounded-full bg-gray-200 overflow-hidden">
            <div
              className="h-full rounded-full bg-[#00B074] transition-all"
              style={{ width: `${progress.progressPercent}%` }}
            />
          </div>
          <div className="flex items-center space-x-2 text-xs font-semibold text-gray-500">
            {progress.completed ? <CheckCircle2 className="w-4 h-4 text-[#00B074]" /> : null}
            <span>{progress.completed ? 'Completed' : `${progress.pendingRecords} pending`}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ActiveBatchCard;
