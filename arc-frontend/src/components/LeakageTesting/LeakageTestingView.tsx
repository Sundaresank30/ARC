import React from 'react';
import { 
  Calendar, 
  FileText, 
  ShieldCheck, 
  ArrowRight, 
  AlertCircle, 
  Clock, 
  Info, 
  ArrowDown, 
  ArrowUp, 
  Loader2 
} from 'lucide-react';

export interface LeakageTestRecord {
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Passed';
  testValue: number;
  direction: 'up' | 'down';
  timestamp: string;
  attempt: string;
  action: 'Scrap' | 'Pending';
}

const mockFailures: LeakageTestRecord[] = [
  {
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
    partNo: 'Pn00113c',
    serialNo: 'P0011158',
    status: 'Failed',
    testValue: 0.48,
    direction: 'down',
    timestamp: '18:00, 20 Jul',
    attempt: '1/2',
    action: 'Pending',
  },
];

export const LeakageTestingView: React.FC = () => {
  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Workspace Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight">
            Leakage Testing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
            Monitor automated leakage testing and inspection status
          </p>
        </div>

        {/* Date Selector Badge */}
        <div className="flex items-center space-x-2 bg-white border border-gray-200 px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-semibold text-gray-700">20 July, 2026</span>
        </div>
      </div>

      {/* Overview Cards Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Active Batch Card */}
        <div className="md:col-span-2 bg-white border border-gray-200/80 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <h2 className="text-base font-bold text-gray-900 mb-6">
            Active Batch
          </h2>

          <div className="flex items-center space-x-4 py-2">
            {/* Batch Item */}
            <div className="flex items-center space-x-2 text-emerald-600 bg-emerald-50/60 px-3 py-1.5 rounded-lg border border-emerald-100/80">
              <FileText className="w-4 h-4 text-emerald-600" />
              <span className="text-sm font-semibold">Batch_1</span>
            </div>

            {/* Stepper Arrow Line */}
            <div className="flex-1 flex items-center justify-center relative">
              <div className="w-full border-t border-gray-200 flex items-center justify-center">
                <ArrowRight className="w-4 h-4 text-gray-400 absolute bg-white px-0.5" />
              </div>
            </div>

            {/* Step Target */}
            <div className="flex items-center space-x-2 text-gray-400 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-200/60">
              <ShieldCheck className="w-4 h-4 text-gray-400" />
              <span className="text-sm font-semibold text-gray-400">Leakage Testing</span>
            </div>
          </div>
        </div>

        {/* Failed KPI Card */}
        <div className="bg-[#FFF5F5] border border-red-100 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div className="flex items-center space-x-2 text-red-600 font-bold text-base">
            <AlertCircle className="w-5 h-5 text-red-500" />
            <span>Failed</span>
          </div>

          <div className="my-2">
            <span className="text-4xl font-extrabold text-red-500 tracking-tight">
              3
            </span>
          </div>

          <div className="text-xs font-semibold text-red-400">
            -from this batch
          </div>
        </div>
      </div>

      {/* Main Leakage Inspection Results Card */}
      <div className="bg-white border border-gray-200/80 rounded-3xl p-6 sm:p-8 shadow-sm">
        {/* Card Section Header */}
        <div className="flex items-center space-x-2 mb-6">
          <h2 className="text-xl font-bold text-gray-900 tracking-tight">
            Leakage Inspection Results
          </h2>
          <Info className="w-4 h-4 text-gray-400" />
        </div>

        {/* Inner Content Area */}
        <div className="border border-gray-100 rounded-2xl overflow-hidden shadow-sm space-y-4 p-4 sm:p-6 bg-white">
          
          {/* Leaked Testing Failures Red Alert Header */}
          <div className="bg-red-50 border border-red-100/70 rounded-xl p-3.5 sm:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center space-x-2 text-red-600 font-bold text-sm sm:text-base">
              <Clock className="w-4 h-4 text-red-500" />
              <span>Leaked Testing Failures</span>
            </div>

            <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
              <span className="text-red-500 bg-red-100/60 px-3 py-1 rounded-md">
                Requires quality action
              </span>
              <span className="text-red-500 bg-red-100/60 px-3 py-1 rounded-md">
                Threshold Range: 0.50 – 1.00
              </span>
            </div>
          </div>

          {/* Batch Progress Banner Pill */}
          <div className="bg-emerald-50/80 border border-emerald-100 rounded-xl px-4 py-3 flex items-center justify-between">
            <span className="text-xs sm:text-sm font-semibold text-emerald-800">
              Batch Progress: 97% Complete (97 / 100 Parts)
            </span>
            <Loader2 className="w-4 h-4 text-emerald-600 animate-spin" />
          </div>

          {/* Table Container */}
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-gray-200/80 text-xs font-semibold text-gray-500 bg-gray-50/50">
                  <th className="py-3 px-4">Part no.</th>
                  <th className="py-3 px-4">Serial no.</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Test Value</th>
                  <th className="py-3 px-4">Timestamp</th>
                  <th className="py-3 px-4">Attempt</th>
                  <th className="py-3 px-4">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 text-xs sm:text-sm font-medium text-gray-700">
                {mockFailures.map((item, idx) => (
                  <tr key={idx} className="hover:bg-gray-50/60 transition-colors">
                    <td className="py-3.5 px-4 font-semibold text-gray-600">
                      {item.partNo}
                    </td>
                    <td className="py-3.5 px-4 text-gray-600">
                      {item.serialNo}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="bg-red-50 text-red-500 border border-red-100 px-2.5 py-1 rounded-md text-xs font-semibold">
                        {item.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-red-500 flex items-center space-x-1">
                      <span>{item.testValue.toFixed(2)}</span>
                      {item.direction === 'down' ? (
                        <ArrowDown className="w-3.5 h-3.5 text-red-500" />
                      ) : (
                        <ArrowUp className="w-3.5 h-3.5 text-red-500" />
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-gray-500">
                      {item.timestamp}
                    </td>
                    <td className="py-3.5 px-4 text-gray-500">
                      {item.attempt}
                    </td>
                    <td className="py-3.5 px-4 font-semibold text-gray-600">
                      {item.action}
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

export default LeakageTestingView;
