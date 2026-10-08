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
    <div className="bg-[#121417] border border-[#1e232a] rounded-2xl p-6 shadow-[0_0_20px_rgba(0,216,246,0.1)] flex flex-col justify-between h-full">
      <div className="flex items-center justify-between mb-5">
        <h2 className="text-base font-semibold text-white">
          Active Batch
        </h2>
        {progress && (
          <span className="text-sm font-semibold text-[#00d8f6]">
            {progress.progressPercent}% Complete
          </span>
        )}
      </div>

      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2 text-[#00d8f6] bg-[#19272e] px-3 py-1.5 rounded-lg border border-[#00d8f6]/30">
          <FileText className="w-4 h-4 text-[#00d8f6]" />
          {isLoading ? (
            <Loader2 className="w-4 h-4 animate-spin text-[#00d8f6]" />
          ) : (
            <span className="text-sm font-semibold">{activeBatch}</span>
          )}
        </div>

        <div className="flex-1 flex items-center max-w-[180px]">
          <div className="w-1.5 h-1.5 rounded-full bg-[#2d333c] shrink-0" />
          <div className="flex-1 h-[1.5px] bg-[#2d333c]" />
          <div className="w-2 h-2 border-t-[1.5px] border-r-[1.5px] border-[#2d333c] transform rotate-45 -ml-1 shrink-0" />
        </div>

        <div className="flex items-center space-x-2">
          <div className="w-9 h-9 rounded-lg bg-[#181a1d] border border-[#2d333c] flex items-center justify-center text-gray-400">
            <Database className="w-5 h-5 stroke-[2]" />
          </div>
          <span className="text-base font-bold text-gray-400">Data Embossing</span>
        </div>
      </div>
    </div>
  );
};

export default ActiveBatchCard;
