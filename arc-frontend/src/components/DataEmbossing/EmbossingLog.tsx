import React, { useState } from 'react';
import { Info, Clock, Loader2 } from 'lucide-react';

interface EmbossingRowItem {
  id: string;
  partNo: string;
  serialNo: string;
  status: 'Pending' | 'Queued' | 'Completed';
  remainingSince: string;
  nextShift: string;
  action: string;
}

export const EmbossingLog: React.FC = () => {
  const [embossingData, setEmbossingData] = useState<EmbossingRowItem[]>([
    {
      id: '1',
      partNo: 'Pn00111c',
      serialNo: 'P0011156',
      status: 'Pending',
      remainingSince: '17:57, 20 Jul',
      nextShift: '21 Jul',
      action: 'Queued',
    },
    {
      id: '2',
      partNo: 'Pn00112c',
      serialNo: 'P0011157',
      status: 'Pending',
      remainingSince: '17:58, 20 Jul',
      nextShift: '21 Jul',
      action: 'Queued',
    },
    {
      id: '3',
      partNo: 'Pn00113c',
      serialNo: 'P0011158',
      status: 'Pending',
      remainingSince: '18:00, 20 Jul',
      nextShift: '21 Jul',
      action: 'Queued',
    },
  ]);

  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const triggerToast = (message: string) => {
    setToastMessage(message);
    setTimeout(() => {
      setToastMessage(null);
    }, 3000);
  };

  const handleAction = (id: string, partNo: string, action: string) => {
    triggerToast(`Action "${action}" triggered for Part ${partNo}`);
  };

  return (
    <div className="bg-white rounded-3xl p-6 sm:p-8 border border-gray-150 shadow-sm relative">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="absolute top-4 right-4 bg-gray-900 text-white text-xs font-semibold px-4 py-2.5 rounded-lg shadow-lg z-50 animate-fade-in">
          {toastMessage}
        </div>
      )}

      {/* Title Header */}
      <div className="flex items-center space-x-2 mb-6">
        <h2 className="text-xl font-bold text-gray-900 tracking-tight">
          Embossing Log
        </h2>
        <Info className="w-4 h-4 text-gray-400 cursor-pointer hover:text-gray-600" />
      </div>

      {/* Main Table Box */}
      <div className="border border-amber-100 rounded-2xl overflow-hidden shadow-sm">
        {/* Table Header Bar */}
        <div className="bg-[#FFFDF5] border-b border-amber-100 px-4 py-3 flex items-center justify-between">
          <div className="flex items-center space-x-2 text-amber-700">
            <Clock className="w-4.5 h-4.5 stroke-[2.5]" />
            <span className="font-bold text-sm sm:text-base">
              Carry Forward (Embossing)
            </span>
          </div>
          <span className="text-xs sm:text-sm font-semibold text-amber-600/90">
            Scheduled for next shift
          </span>
        </div>

        {/* Table layout */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs sm:text-sm border-collapse">
            <thead>
              <tr className="bg-white text-gray-400 font-semibold border-b border-gray-100">
                <th className="px-4 py-3.5 font-semibold text-gray-500">Part no.</th>
                <th className="px-4 py-3.5 font-semibold text-gray-500">Serial no.</th>
                <th className="px-4 py-3.5 font-semibold text-gray-500">Status</th>
                <th className="px-4 py-3.5 font-semibold text-gray-500">Remaining Since</th>
                <th className="px-4 py-3.5 font-semibold text-gray-500">Next Shift</th>
                <th className="px-4 py-3.5 font-semibold text-gray-500">Action</th>
              </tr>
            </thead>
            <tbody>
              {/* Batch Progress Banner (Green Bar spanning all columns) */}
              <tr className="bg-[#EBFDF5] border-b border-gray-100">
                <td colSpan={6} className="px-4 py-3">
                  <div className="flex items-center space-x-2 text-[#00B074]">
                    <span className="text-xs sm:text-sm font-bold">
                      Batch Progress: 97% Complete (97 / 100 Parts)
                    </span>
                    <Loader2 className="w-4 h-4 animate-spin text-[#00B074]" />
                  </div>
                </td>
              </tr>

              {/* Data Rows */}
              {embossingData.map((row) => (
                <tr key={row.id} className="bg-white hover:bg-gray-50/50 transition-colors border-b border-gray-100 last:border-b-0">
                  <td className="px-4 py-4 font-semibold text-gray-600">{row.partNo}</td>
                  <td className="px-4 py-4 text-gray-600 font-medium">{row.serialNo}</td>
                  <td className="px-4 py-4">
                    <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#FEF3C7] text-[#D97706]">
                      {row.status}
                    </span>
                  </td>
                  <td className="px-4 py-4 text-gray-500 font-medium">{row.remainingSince}</td>
                  <td className="px-4 py-4 text-gray-500 font-medium">{row.nextShift}</td>
                  <td className="px-4 py-4">
                    <button
                      onClick={() => handleAction(row.id, row.partNo, row.action)}
                      className="font-bold text-gray-600 hover:text-indigo-600 transition-colors"
                    >
                      {row.action}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
export default EmbossingLog;
