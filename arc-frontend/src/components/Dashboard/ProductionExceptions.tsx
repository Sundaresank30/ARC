import React, { useState } from 'react';
import { Info, Clock, AlertTriangle, ChevronDown, ChevronUp } from 'lucide-react';

interface CarryForwardItem {
  id: string;
  partNo: string;
  serialNo: string;
  status: 'Pending' | 'Queued' | 'Completed' | string;
  remainingSince: string;
  nextShift: string;
  action: string;
}

interface LeakageFailureItem {
  id: string;
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
    <div className="bg-[#0D0E19] rounded-3xl p-6 sm:p-8 border border-[#1b172a] shadow-sm relative">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="absolute top-4 right-4 bg-[#1a162b] text-white text-xs font-semibold px-4 py-2.5 rounded-lg shadow-lg border border-[#3b2d6a] z-50 animate-fade-in">
          {toastMessage}
        </div>
      )}

      {/* Main Title Header */}
      <div className="flex items-center space-x-2 mb-6">
        <h2 className="text-xl font-bold text-white tracking-tight">
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
              Scheduled for next shift
            </span>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-[#0D0E19] text-[#8a8596] font-semibold border-b border-[#221e33]">
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Remaining Since</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Next Shift</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1c182a] bg-[#0D0E19]">
                {carryForwardData.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-xs text-gray-500 font-medium">
                      No pending carry-forward embossing exceptions recorded.
                    </td>
                  </tr>
                ) : (
                  carryForwardData.map((row) => (
                    <tr key={row.id} className="hover:bg-[#120e21]/50 transition-colors">
                      <td className="px-4 py-4 font-semibold text-white">{row.partNo}</td>
                      <td className="px-4 py-4 text-gray-300 font-medium">{row.serialNo}</td>
                      <td className="px-4 py-4">
                        <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#2d1c0b] text-[#f59e0b]">
                          {row.status}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-gray-400 font-medium">{row.remainingSince}</td>
                      <td className="px-4 py-4 text-gray-400 font-medium">{row.nextShift}</td>
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
                <tr className="bg-[#0D0E19] text-[#8a8596] font-semibold border-b border-[#221e33]">
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Test Value</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Timestamp</th>
                  <th className="px-4 py-3.5 font-semibold text-[#8a8596]">Attempt</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1c182a] bg-[#0D0E19]">
                {leakageFailuresData.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-xs text-gray-500 font-medium">
                      No leakage inspection failures recorded.
                    </td>
                  </tr>
                ) : (
                  leakageFailuresData.map((row) => (
                    <tr key={row.id} className="hover:bg-[#120e21]/50 transition-colors">
                      <td className="px-4 py-4 font-semibold text-white">{row.partNo}</td>
                      <td className="px-4 py-4 text-gray-300 font-medium">{row.serialNo}</td>
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
