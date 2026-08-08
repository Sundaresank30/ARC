import React from 'react';
import {
  Calendar,
  FileText,
  ShieldCheck,
  AlertCircle,
  Clock,
  Info,
  ArrowDown,
  ArrowUp,
  Loader2,
  CheckCircle2,
} from 'lucide-react';
import {
  useLeakageDashboard,
  useUpdateLeakageAction,
  useLeakageProgressSocket,
} from '../../api/leakageTesting';
import { LeakageTestRecord } from '../../types';

export const LeakageTestingView: React.FC = () => {
  // Use React Query and WebSockets
  useLeakageProgressSocket(true);
  const { data: dashboard, isLoading } = useLeakageDashboard(true);
  const updateActionMutation = useUpdateLeakageAction();

  const data = dashboard || {
    activeBatch: 'Batch_1',
    failedCount: 0,
    passedCount: 0,
    batchProgressPercent: 0,
    completedCount: 0,
    totalParts: 0,
    dateDisplay: '',
    batchStatus: 'No Batch',
    failures: [],
    passed: [],
  };

  const handleActionToggle = async (record: LeakageTestRecord) => {
    if (!record.id) return;
    const nextAction = record.action === 'Scrap' ? 'Pending' : 'Scrap';
    updateActionMutation.mutate({ id: record.id, action: nextAction });
  };

  const isBatchCompleted = data.batchProgressPercent >= 100 || data.batchStatus === '100% completed';
  const passedResults = data.passed || [];
  const failureResults = data.failures || [];

  // Helper render component for Green Batch Progress Bar
  const renderBatchProgressBar = () => {
    const percent = data.batchProgressPercent;
    const completedCount = data.completedCount;
    const totalCount = data.totalParts;
    const isActive = !isBatchCompleted;

    return (
      <div
        className={`relative w-full h-9 bg-[#041a13] rounded-lg overflow-hidden border border-[#00B074]/20 flex items-center transition-all duration-300 ${isActive ? 'animate-progress-active' : ''}`}
      >
        <div
          className="absolute top-0 left-0 h-full bg-[#00B074]/60 transition-[width] duration-500 ease-out"
          style={{ width: `${percent}%` }}
        />
        <div className="absolute inset-0 flex items-center justify-between px-4 z-10">
          <span className="text-xs sm:text-sm font-semibold text-emerald-400">
            Batch Progress: {percent}% Complete ({completedCount} / {totalCount} Parts)
          </span>
          <div className="flex items-center space-x-2 text-emerald-400">
            {isActive && (
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
            )}
            <span className="text-xs sm:text-sm font-bold">
              {percent}%
            </span>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Workspace Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-semibold text-white tracking-tight leading-tight">
            Leakage Testing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Review automated leakage inspection test results and monitor quality actions in real-time
          </p>
        </div>

        {/* Date Badge - Real-time Current Day */}
        <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm font-semibold text-gray-300">
            {new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })}
          </span>
        </div>
      </div>

      {/* Overview Cards Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Active Batch Card */}
        <div className="lg:col-span-2 bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full hover:shadow-md transition-shadow duration-150">
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-base font-semibold text-white">
              Active Batch
            </h2>
            {data.batchProgressPercent !== undefined && (
              <span className="text-sm font-semibold text-[#00B074]">
                {data.batchProgressPercent}% Complete
              </span>
            )}
          </div>

          <div className="flex items-center space-x-4">
            {/* Batch Item */}
            <div className="flex items-center space-x-2 text-emerald-400 bg-[#0a231b] px-3 py-1.5 rounded-lg border border-emerald-950/40">
              <FileText className="w-4 h-4 text-emerald-400" />
              <span className="text-sm font-semibold">{data.activeBatch}</span>
            </div>

            {/* Stepper Arrow Line */}
            <div className="flex-1 flex items-center max-w-[180px]">
              <div className="w-1.5 h-1.5 rounded-full bg-[#1e1b29] shrink-0" />
              <div className="flex-1 h-[1.5px] bg-[#1e1b29]" />
              <div className="w-2 h-2 border-t-[1.5px] border-r-[1.5px] border-[#1e1b29] transform rotate-45 -ml-1 shrink-0" />
            </div>

            {/* Step Target */}
            <div className="flex items-center space-x-2">
              <div className="w-9 h-9 rounded-lg bg-[#13111c] border border-[#221e33] flex items-center justify-center text-[#8a8596]">
                <ShieldCheck className="w-5 h-5 stroke-[2]" />
              </div>
              <span className="text-base font-bold text-gray-500">Leakage Testing</span>
            </div>
          </div>
        </div>

        {/* Failed KPI Card (Monitored in real-time) */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full hover:shadow-md transition-shadow duration-150">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-[15px] text-gray-300">Failed</span>
            <div className="w-8 h-8 rounded-lg bg-[#271012] border border-[#ef4444]/20 flex items-center justify-center text-red-500">
              <AlertCircle className="w-4.5 h-4.5" />
            </div>
          </div>

          <div className="my-1">
            <span className="text-[44px] font-bold leading-none tracking-tight text-gray-300">
              {data.failedCount}
            </span>
          </div>

          <div>
            <span className="text-xs font-semibold text-red-500">-from active batch</span>
          </div>
        </div>
      </div>

      {/* Main Leakage Inspection Results Card */}
      <div className="bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 sm:p-8 shadow-sm">
        {/* Card Section Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-2">
            <h2 className="text-xl font-semibold text-white tracking-tight">
              Leakage Inspection Results
            </h2>
            <Info className="w-4 h-4 text-gray-500" />
          </div>
        </div>

        {/* Inner Content Area */}
        <div className="space-y-6">

          {/* ----------------------------------------------------------------- */}
          {/* TABLE 1: LEAKED TESTING FAILURES */}
          {/* ----------------------------------------------------------------- */}
          <div className="border border-[#ef4444]/30 rounded-2xl overflow-hidden shadow-sm">
            {/* Header Alert */}
            <div className="bg-[#271012]/50 border-b border-[#ef4444]/30 px-4 py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center space-x-2 text-[#ef4444]/90 font-bold text-sm sm:text-base">
                <Clock className="w-4 h-4 text-[#ef4444]/90" />
                <span>Leaked Testing Failures ({failureResults.length})</span>
              </div>

              <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
                <span className="text-[#ef4444]/90 bg-[#271012]/60 px-3 py-1 rounded-md border border-[#ef4444]/20">
                  Requires quality action
                </span>
                <span className="text-[#ef4444]/90 bg-[#271012]/60 px-3 py-1 rounded-md border border-[#ef4444]/20">
                  Threshold Range: 75.0 – 80.0 kPa
                </span>
              </div>
            </div>

            {/* Table Container */}
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs sm:text-sm border-collapse">
                <thead>
                  <tr className="bg-[#13111c]/30 text-gray-500 font-semibold border-b border-[#221e33]">
                    <th className="py-3.5 px-4">Part no.</th>
                    <th className="py-3.5 px-4">Serial no.</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4">Test Value</th>
                    <th className="py-3.5 px-4">Timestamp</th>
                    <th className="py-3.5 px-4">Attempt</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1b172a] text-xs sm:text-sm font-medium text-gray-300">
                  {/* Batch Progress Bar */}
                  <tr>
                    <td colSpan={6} className="px-4 py-3">
                      {renderBatchProgressBar()}
                    </td>
                  </tr>

                  {isLoading ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-8 text-center">
                        <Loader2 className="w-6 h-6 animate-spin text-gray-400 mx-auto" />
                      </td>
                    </tr>
                  ) : failureResults.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="py-8 text-center text-gray-500 font-medium bg-[#13111c]/10">
                        <div className="flex flex-col items-center justify-center space-y-1.5">
                          <CheckCircle2 className="w-6 h-6 text-emerald-500" />
                          <span className="text-xs text-gray-400 font-semibold">
                            No leakage inspection failures recorded for active batch
                          </span>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    failureResults.map((item, idx) => (
                      <tr key={item.id ?? idx} className="bg-[#0D0E19] hover:bg-[#151221]/30 transition-colors border-b border-[#1b172a] last:border-b-0">
                        <td className="px-4 py-4 font-semibold text-gray-400 font-mono">
                          {item.partNo}
                        </td>
                        <td className="px-4 py-4 text-gray-400 font-mono">
                          {item.serialNo}
                        </td>
                        <td className="px-4 py-4">
                          <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-[#271012] text-red-400">
                            {item.status}
                          </span>
                        </td>
                        <td className="px-4 py-4 font-mono">
                          <div className="flex items-center space-x-1 font-bold">
                            <span className="text-red-400">{typeof item.testValue === 'number' ? item.testValue.toFixed(2) : item.testValue} kPa</span>
                            {item.direction === 'down' ? (
                              <ArrowDown className="w-3.5 h-3.5 text-red-500" />
                            ) : (
                              <ArrowUp className="w-3.5 h-3.5 text-red-500" />
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-4 text-gray-500">
                          {item.timestamp}
                        </td>
                        <td className="px-4 py-4 text-gray-500">
                          {item.attempt}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* ----------------------------------------------------------------- */}
          {/* TABLE 2: PASSED INSPECTION RESULTS */}
          {/* ----------------------------------------------------------------- */}
          <div className="border border-[#00B074]/30 rounded-2xl overflow-hidden shadow-sm">
            {/* Header Success Banner */}
            <div className="bg-[#0a231b]/50 border-b border-[#00B074]/30 px-4 py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center space-x-2 text-emerald-400 font-bold text-sm sm:text-base">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span>Passed Inspection Results ({passedResults.length})</span>
              </div>

              <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
                <span className="text-emerald-400 bg-emerald-950/60 px-3 py-1 rounded-md border border-emerald-900/40">
                  Quality Approved
                </span>
                {data.activeBatch !== 'No Active Batch' && (
                  <span className="text-emerald-400 bg-emerald-950/60 px-3 py-1 rounded-md border border-emerald-900/40">
                    Quality Approved Log
                  </span>
                )}
              </div>
            </div>

            {/* Table Container */}
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs sm:text-sm border-collapse">
                <thead>
                  <tr className="bg-[#13111c]/30 text-gray-500 font-semibold border-b border-[#221e33]">
                    <th className="py-3.5 px-4">Part no.</th>
                    <th className="py-3.5 px-4">Serial no.</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4">Test Value</th>
                    <th className="py-3.5 px-4">Timestamp</th>
                    <th className="py-3.5 px-4">Attempt</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1b172a] text-xs sm:text-sm font-medium text-gray-300">
                  {/* Batch Progress Bar */}
                  <tr>
                    <td colSpan={6} className="px-4 py-3">
                      {renderBatchProgressBar()}
                    </td>
                  </tr>

                  {isLoading ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-8 text-center">
                        <Loader2 className="w-6 h-6 animate-spin text-gray-400 mx-auto" />
                      </td>
                    </tr>
                  ) : passedResults.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="py-8 text-center text-gray-500 font-medium bg-[#13111c]/10">
                        <div className="flex flex-col items-center justify-center space-y-1.5">
                          <Info className="w-6 h-6 text-gray-500" />
                          <span className="text-xs text-gray-400 font-semibold">
                            No passed inspection results recorded for active batch yet
                          </span>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    passedResults.map((item, idx) => (
                      <tr key={item.id ?? idx} className="bg-[#0D0E19] hover:bg-[#151221]/30 transition-colors border-b border-[#1b172a] last:border-b-0">
                        <td className="px-4 py-4 font-semibold text-gray-400 font-mono">
                          {item.partNo}
                        </td>
                        <td className="px-4 py-4 text-gray-400 font-mono">
                          {item.serialNo}
                        </td>
                        <td className="px-4 py-4">
                          <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-emerald-950/60 text-emerald-400">
                            {item.status}
                          </span>
                        </td>
                        <td className="px-4 py-4 font-mono">
                          <div className="flex items-center space-x-1 font-bold">
                            <span className="text-emerald-400">{typeof item.testValue === 'number' ? item.testValue.toFixed(2) : item.testValue} kPa</span>
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                          </div>
                        </td>
                        <td className="px-4 py-4 text-gray-500">
                          {item.timestamp}
                        </td>
                        <td className="px-4 py-4 text-gray-500">
                          {item.attempt}
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
    </div>
  );
};

export default LeakageTestingView;
