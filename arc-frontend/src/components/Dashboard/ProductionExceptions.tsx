import React, { useState } from 'react';
import { Info, Clock, AlertTriangle, ChevronDown, ChevronUp } from 'lucide-react';

interface CarryForwardItem {
  id: string;
  partNo: string;
  serialNo: string;
  status: 'Pending' | 'Queued' | 'Completed' | string;
  remainingSince: string;
  batchId: string;
  action: string;
}

interface LeakageFailureItem {
  id: string;
  batchId?: string;
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Scrap' | 'Pending' | string;
  testValue: number;
  direction: 'up' | 'down' | string;
  timestamp: string;
  attempt: string;
  action: string;
}

interface ProductionExceptionsProps {
  carryForwardData?: CarryForwardItem[];
  leakageFailuresData?: LeakageFailureItem[];
  onResolveCarryForward?: (id: string, partNo: string) => void;
  onResolveLeakage?: (id: string, partNo: string) => void;
}

export const ProductionExceptions: React.FC<ProductionExceptionsProps> = ({
  carryForwardData: initialCarryForward = [],
  leakageFailuresData: initialLeakage = [],
  onResolveCarryForward,
  onResolveLeakage,
}) => {
  const [carryForwardData, setCarryForwardData] = useState<CarryForwardItem[]>(
    initialCarryForward || []
  );

  const [leakageFailuresData, setLeakageFailuresData] = useState<LeakageFailureItem[]>(
    initialLeakage || []
  );

  React.useEffect(() => {
    if (initialCarryForward) setCarryForwardData(initialCarryForward);
  }, [initialCarryForward]);

  React.useEffect(() => {
    if (initialLeakage) setLeakageFailuresData(initialLeakage);
  }, [initialLeakage]);

  // Toast / Action notification
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const triggerToast = (message: string) => {
    setToastMessage(message);
    setTimeout(() => {
      setToastMessage(null);
    }, 3000);
  };

  const handleCarryForwardAction = (id: string, partNo: string, currentAction: string) => {
    if (onResolveCarryForward) {
      onResolveCarryForward(id, partNo);
    }
    triggerToast(`Action "${currentAction}" completed for Part ${partNo}`);
  };

  const handleLeakageAction = (id: string, partNo: string, currentAction: string) => {
    if (onResolveLeakage) {
      onResolveLeakage(id, partNo);
    }
    triggerToast(`Action "${currentAction}" completed for Part ${partNo}`);
  };

  return (
    <div className="bg-[#101010] rounded-3xl p-6 sm:p-8 border border-[#1e232a] relative">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="absolute top-4 right-4 bg-[#18181b] text-white text-xs font-semibold px-4 py-2.5 rounded-lg shadow-lg border border-[#27272a] z-50 animate-fade-in">
          {toastMessage}
        </div>
      )}

      {/* Main Title Header */}
      <div className="flex items-center space-x-2 mb-6">
        <h2 className="text-xl font-semibold text-white tracking-tight">
          Production Exceptions
        </h2>
        <Info className="w-4 h-4 text-gray-500 cursor-pointer hover:text-gray-300" />
      </div>

      <div className="space-y-8">

        {/* Section 1: Carry Forward (Embossing) */}
        <div className="border border-[#f59e0b]/20 rounded-2xl overflow-hidden shadow-sm">
          {/* Section Header */}
          <div className="bg-[#20150b]/50 border-b border-[#f59e0b]/30 px-4 py-3 flex items-center justify-between">
            <div className="flex items-center space-x-2 text-[#f59e0b]">
              <Clock className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-bold text-sm sm:text-base">
                Carry Forward (Embossing)
              </span>
            </div>
            <span className="text-xs font-semibold text-[#f59e0b]/90">
              Active carry-forward embossing
            </span>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-[#18181b] text-[#9ca3af] font-semibold border-b border-[#232328]">
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Remaining Since</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Batch ID</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1f1f25] bg-[#141619]">
                {carryForwardData.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-gray-500 font-medium bg-[#141414]/30">
                      <div className="flex flex-col items-center justify-center space-y-1.5">
                        <Info className="w-6 h-6 text-gray-500" />
                        <span className="text-xs text-gray-500 font-semibold">
                          No pending carry-forward embossing exceptions recorded
                        </span>
                      </div>
                    </td>
                  </tr>
                ) : (
                  carryForwardData.map((row) => (
                    <tr key={row.id} className="hover:bg-[#1a1d22] transition-colors">
                      <td className="px-4 py-4 font-semibold text-white">{row.partNo}</td>
                      <td className="px-4 py-4 text-gray-300 font-medium">{row.serialNo}</td>
                      <td className="px-4 py-4">
                        <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#2d1c0b] text-[#f59e0b]">
                          {row.status}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-gray-400 font-medium">{row.remainingSince}</td>
                      <td className="px-4 py-4 text-gray-400 font-medium">{row.batchId}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Section 2: Leaked Testing Failures */}
        <div className="border border-[#ef4444]/30 rounded-2xl overflow-hidden shadow-sm">
          {/* Section Header */}
          <div className="bg-[#271012]/50 border-b border-[#ef4444]/30 px-4 py-3 flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center space-x-2 text-[#ef4444]">
              <AlertTriangle className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-bold text-sm sm:text-base">
                Leaked Testing Failures
              </span>
            </div>
            <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
              <span className="text-[#ef4444]/90">
                Requires quality action
              </span>
              <span className="text-[#ef4444]/90 bg-[#271012]/60 px-3 py-1 rounded-md border border-[#ef4444]/30">
                Threshold Range: 75.0 – 80.0 kPa
              </span>
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-[#18181b] text-[#9ca3af] font-semibold border-b border-[#232328]">
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Batch ID</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Test Value</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Timestamp</th>
                  <th className="px-4 py-3.5 font-semibold text-[#9ca3af]">Attempt</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1f1f25] bg-[#141619]">
                {leakageFailuresData.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-8 text-center text-gray-500 font-medium bg-[#141414]/30">
                      <div className="flex flex-col items-center justify-center space-y-1.5">
                        <Info className="w-6 h-6 text-gray-500" />
                        <span className="text-xs text-gray-500 font-semibold">
                          No leakage inspection failures recorded
                        </span>
                      </div>
                    </td>
                  </tr>
                ) : (
                  leakageFailuresData.map((row) => (
                    <tr key={row.id} className="hover:bg-[#1a1d22] transition-colors">
                      <td className="px-4 py-4 font-semibold text-white">{row.partNo}</td>
                      <td className="px-4 py-4 text-gray-300 font-medium">{row.serialNo}</td>
                      <td className="px-4 py-4 text-gray-300 font-medium">{row.batchId || 'N/A'}</td>
                      <td className="px-4 py-4">
                        <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#3a1012] text-[#ef4444]">
                          {row.status}
                        </span>
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex items-center space-x-1 text-[#ef4444] font-bold">
                          <span>{typeof row.testValue === 'number' ? row.testValue.toFixed(2) : row.testValue}</span>
                          {row.direction === 'up' ? (
                            <ChevronUp className="w-4 h-4 text-[#ef4444] stroke-[3]" />
                          ) : (
                            <ChevronDown className="w-4 h-4 text-[#ef4444] stroke-[3]" />
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-gray-400 font-medium">{row.timestamp}</td>
                      <td className="px-4 py-4 text-gray-400 font-semibold">{row.attempt}</td>
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
export default ProductionExceptions;
