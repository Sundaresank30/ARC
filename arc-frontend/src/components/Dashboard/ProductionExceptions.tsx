import React, { useState } from 'react';
import { Info, Clock, AlertTriangle, ChevronDown, ChevronUp } from 'lucide-react';

interface CarryForwardItem {
  id: string;
  partNo: string;
  serialNo: string;
  status: 'Pending' | 'Queued' | 'Completed';
  remainingSince: string;
  nextShift: string;
  action: string;
}

interface LeakageFailureItem {
  id: string;
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Scrap' | 'Pending';
  testValue: number;
  direction: 'up' | 'down';
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
  carryForwardData: initialCarryForward,
  leakageFailuresData: initialLeakage,
  onResolveCarryForward,
  onResolveLeakage,
}) => {
  const [carryForwardData, setCarryForwardData] = useState<CarryForwardItem[]>(
    initialCarryForward || [
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
    ]
  );

  const [leakageFailuresData, setLeakageFailuresData] = useState<LeakageFailureItem[]>(
    initialLeakage || [
      {
        id: '1',
        partNo: 'Pn00111c',
        serialNo: 'P0011156',
        status: 'Failed',
        testValue: 0.42,
        direction: 'down',
        timestamp: '17:57, 20 Jul',
        attempt: '2/2',
        action: 'Scrap',
      },
      {
        id: '2',
        partNo: 'Pn00112c',
        serialNo: 'P0011157',
        status: 'Failed',
        testValue: 1.08,
        direction: 'up',
        timestamp: '17:58, 20 Jul',
        attempt: '1/2',
        action: 'Pending',
      },
      {
        id: '3',
        partNo: 'Pn00113c',
        serialNo: 'P0011158',
        status: 'Failed',
        testValue: 0.48,
        direction: 'down',
        timestamp: '18:00, 20 Jul',
        attempt: '1/2',
        action: 'Pending',
      },
    ]
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
    <div className="bg-white rounded-3xl p-6 sm:p-8 border border-gray-150 shadow-sm relative">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="absolute top-4 right-4 bg-gray-900 text-white text-xs font-semibold px-4 py-2.5 rounded-lg shadow-lg z-50 animate-fade-in">
          {toastMessage}
        </div>
      )}

      {/* Main Title Header */}
      <div className="flex items-center space-x-2 mb-6">
        <h2 className="text-xl font-bold text-gray-900 tracking-tight">
          Production Exceptions
        </h2>
        <Info className="w-4 h-4 text-gray-400 cursor-pointer hover:text-gray-600" />
      </div>

      <div className="space-y-8">
        
        {/* Section 1: Carry Forward (Embossing) */}
        <div className="border border-amber-100 rounded-2xl overflow-hidden shadow-sm">
          {/* Section Header */}
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

          {/* Table */}
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
              <tbody className="divide-y divide-gray-100 bg-white">
                {carryForwardData.map((row) => (
                  <tr key={row.id} className="hover:bg-gray-50/50 transition-colors">
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
                        onClick={() => handleCarryForwardAction(row.id, row.partNo, row.action)}
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

        {/* Section 2: Leaked Testing Failures */}
        <div className="border border-red-100 rounded-2xl overflow-hidden shadow-sm">
          {/* Section Header */}
          <div className="bg-[#FFF5F5] border-b border-red-100 px-4 py-3 flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center space-x-2 text-red-700">
              <AlertTriangle className="w-4.5 h-4.5 stroke-[2.5]" />
              <span className="font-bold text-sm sm:text-base">
                Leaked Testing Failures
              </span>
            </div>
            <div className="flex items-center space-x-3">
              <span className="text-xs sm:text-sm font-semibold text-red-600/90">
                Requires quality action
              </span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#FEE2E2] text-red-700 border border-red-200">
                Threshold Range: 0.50 – 1.00
              </span>
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="bg-white text-gray-400 font-semibold border-b border-gray-100">
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Part no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Serial no.</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Status</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Test Value</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Timestamp</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Attempt</th>
                  <th className="px-4 py-3.5 font-semibold text-gray-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white">
                {leakageFailuresData.map((row) => (
                  <tr key={row.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-4 font-semibold text-gray-600">{row.partNo}</td>
                    <td className="px-4 py-4 text-gray-600 font-medium">{row.serialNo}</td>
                    <td className="px-4 py-4">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#FEE2E2] text-red-700">
                        {row.status}
                      </span>
                    </td>
                    <td className="px-4 py-4">
                      <div className="flex items-center space-x-1 text-red-600 font-bold">
                        <span>{row.testValue.toFixed(2)}</span>
                        {row.direction === 'up' ? (
                          <ChevronUp className="w-4 h-4 text-red-600 stroke-[3]" />
                        ) : (
                          <ChevronDown className="w-4 h-4 text-red-600 stroke-[3]" />
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-4 text-gray-500 font-medium">{row.timestamp}</td>
                    <td className="px-4 py-4 text-gray-500 font-semibold">{row.attempt}</td>
                    <td className="px-4 py-4">
                      <button
                        onClick={() => handleLeakageAction(row.id, row.partNo, row.action)}
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
    </div>
  );
};
export default ProductionExceptions;
