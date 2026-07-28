import React from 'react';
import { Calendar, AlertCircle } from 'lucide-react';
import { ActiveBatchCard } from './ActiveBatchCard';
import { EmbossingLog } from './EmbossingLog';

export const DataEmbossingPage: React.FC = () => {
  return (
    <div className="animate-fade-in space-y-6">
      
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight">
            Data Embossing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
            Monitor automated data embossing and production status
          </p>
        </div>

        {/* Date Widget */}
        <div className="flex items-center space-x-2 bg-white border border-gray-150 px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-semibold text-gray-700">
            20 July, 2026
          </span>
        </div>
      </div>

      {/* KPI Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Active Batch Connector Card */}
        <div className="lg:col-span-2">
          <ActiveBatchCard />
        </div>

        {/* Pending Card */}
        <div className="lg:col-span-1">
          <div className="bg-white border border-gray-150 rounded-2xl p-6 shadow-sm flex flex-col justify-between min-h-[135px] hover:shadow-md transition-shadow duration-150">
            {/* Header: Icon & Title */}
            <div className="flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-[#D97706]" />
              <span className="font-bold text-[15px] text-[#D97706]">
                Pending
              </span>
            </div>

            {/* Large Count */}
            <div className="my-1">
              <span className="text-[44px] font-bold leading-none tracking-tight text-[#D97706]">
                3
              </span>
            </div>

            {/* Subtext */}
            <div>
              <span className="text-xs font-semibold text-[#D97706]/85">
                -from this batch
              </span>
            </div>
          </div>
        </div>

      </div>

      {/* Embossing Exceptions Log */}
      <EmbossingLog />

    </div>
  );
};
export default DataEmbossingPage;
