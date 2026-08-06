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
    <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full">
      <div className="flex items-center justify-between mb-5">
        <h2 className="text-base font-semibold text-white">
          Active Batch
        </h2>
        {progress && (
          <span className="text-sm font-semibold text-[#00B074]">
            {progress.progressPercent}% Complete
          </span>
        )}
      </div>

      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2 text-emerald-400 bg-[#0a231b] px-3 py-1.5 rounded-lg border border-emerald-950/40">
          <FileText className="w-4 h-4 text-emerald-400" />
          {isLoading ? (
            <Loader2 className="w-4 h-4 animate-spin text-emerald-400" />
          ) : (
            <span className="text-sm font-semibold">{activeBatch}</span>
          )}
        </div>

        <div className="flex-1 flex items-center max-w-[180px]">
          <div className="w-1.5 h-1.5 rounded-full bg-[#1e1b29] shrink-0" />
          <div className="flex-1 h-[1.5px] bg-[#1e1b29]" />
          <div className="w-2 h-2 border-t-[1.5px] border-r-[1.5px] border-[#1e1b29] transform rotate-45 -ml-1 shrink-0" />
        </div>

        <div className="flex items-center space-x-2">
          <div className="w-9 h-9 rounded-lg bg-[#13111c] border border-[#221e33] flex items-center justify-center text-gray-500">
            <Database className="w-5 h-5 stroke-[2]" />
          </div>
          <span className="text-base font-bold text-gray-500">Data Embossing</span>
        </div>
      </div>
    </div>
  );
};

export default ActiveBatchCard;
