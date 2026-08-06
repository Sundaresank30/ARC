import React, { useEffect, useState } from 'react';
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
  Loader2,
  CheckCircle2,
} from 'lucide-react';

export interface LeakageTestRecord {
  id?: number;
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Passed';
  testValue: number;
  direction: 'up' | 'down';
  timestamp: string;
  attempt: string;
  action: 'Scrap' | 'Pending' | string;
}

export interface LeakageTestingDashboardData {
  activeBatch: string;
  failedCount: number;
  batchProgressPercent: number;
  completedCount: number;
  totalParts: number;
  dateDisplay: string;
  failures: LeakageTestRecord[];
}

export const LeakageTestingView: React.FC = () => {
  const [data, setData] = useState<LeakageTestingDashboardData>({
    activeBatch: 'Batch_1',
    failedCount: 0,
    batchProgressPercent: 100,
    completedCount: 100,
    totalParts: 100,
    dateDisplay: '20 July, 2026',
    failures: [],
  });
  const [loading, setLoading] = useState<boolean>(true);

  const fetchDashboardData = async () => {
    try {
      const response = await fetch('/api/leakage-testing');
      if (response.ok) {
        const json: LeakageTestingDashboardData = await response.json();
        setData(json);
      }
    } catch (err) {
      console.warn('Backend unavailable, using current local view state:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleActionToggle = async (record: LeakageTestRecord) => {
    if (!record.id) return;
    const nextAction = record.action === 'Scrap' ? 'Pending' : 'Scrap';

    // Optimistic UI update
    setData((prev) => ({
      ...prev,
      failures: prev.failures.map((f) =>
        f.id === record.id ? { ...f, action: nextAction } : f
      ),
    }));

    try {
      await fetch(`/api/leakage-testing/jobs/${record.id}/action`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: nextAction }),
      });
      fetchDashboardData();
    } catch (err) {
      console.error('Failed to update action on backend:', err);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Workspace Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-white tracking-tight leading-tight">
            Leakage Testing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Review automated leakage inspection failures and take quality actions
          </p>
        </div>

        {/* Date Badge */}
        <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm font-semibold text-gray-300">{data.dateDisplay}</span>
        </div>
      </div>

      {/* Overview Cards Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Active Batch Card */}
        <div className="md:col-span-2 bg-[#0D0E19] border border-[#1b172a] rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <h2 className="text-base font-semibold text-white mb-6">
            Active Batch
          </h2>

          <div className="flex items-center space-x-4 py-2">
            {/* Batch Item */}
            <div className="flex items-center space-x-2 text-emerald-400 bg-[#0a231b] px-3 py-1.5 rounded-lg border border-emerald-950/40">
              <FileText className="w-4 h-4 text-emerald-400" />
              <span className="text-sm font-semibold">{data.activeBatch}</span>
            </div>

            {/* Stepper Arrow Line */}
            <div className="flex-1 flex items-center justify-center relative">
              <div className="w-full border-t border-[#1b172a] flex items-center justify-center">
                <ArrowRight className="w-4 h-4 text-gray-500 absolute bg-[#0D0E19] px-0.5" />
              </div>
            </div>

            {/* Step Target */}
            <div className="flex items-center space-x-2 text-gray-500 bg-[#13111c] px-3 py-1.5 rounded-lg border border-[#221e33]">
              <ShieldCheck className="w-4 h-4 text-gray-500" />
              <span className="text-sm font-semibold text-gray-400">Leakage Testing</span>
            </div>
          </div>
        </div>

        {/* Failed KPI Card */}
        <div className="bg-[#271012] border border-[#ef4444]/20 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div className="flex items-center space-x-2 text-red-500 font-bold text-base">
            <AlertCircle className="w-5 h-5 text-red-500" />
            <span>Failed</span>
          </div>

          <div className="my-2">
            <span className="text-4xl font-extrabold text-red-400 tracking-tight">
              {data.failedCount}
            </span>
          </div>

          <div className="text-xs font-semibold text-red-600/80">
            -from this batch
          </div>
        </div>
      </div>

      {/* Main Leakage Inspection Results Card */}
      <div className="bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 sm:p-8 shadow-sm">
        {/* Card Section Header */}
        <div className="flex items-center space-x-2 mb-6">
          <h2 className="text-xl font-bold text-white tracking-tight">
            Leakage Inspection Results
          </h2>
          <Info className="w-4 h-4 text-gray-500" />
        </div>

        {/* Inner Content Area */}
        <div className="border border-[#1b172a] rounded-2xl overflow-hidden shadow-sm space-y-4 p-4 sm:p-6 bg-[#0D0E19]">

          {/* Leaked Testing Failures Red Alert Header */}
          <div className="bg-[#271012] border border-[#ef4444]/25 rounded-xl p-3.5 sm:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center space-x-2 text-red-400 font-bold text-sm sm:text-base">
              <Clock className="w-4 h-4 text-red-500" />
              <span>Leaked Testing Failures</span>
            </div>

            <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
              <span className="text-red-400 bg-[#ef4444]/15 px-3 py-1 rounded-md border border-[#ef4444]/20">
                Requires quality action
              </span>
              <span className="text-red-400 bg-[#ef4444]/15 px-3 py-1 rounded-md border border-[#ef4444]/20">
                Threshold Range: 75.0 – 80.0 kPa
              </span>
            </div>
          </div>

          {/* Batch Progress Banner Pill */}
          <div className="bg-[#0a231b] border border-emerald-950/40 rounded-xl px-4 py-3 flex items-center justify-between">
            <span className="text-xs sm:text-sm font-semibold text-emerald-400">
              Batch Progress: {data.batchProgressPercent}% Complete ({data.completedCount} / {data.totalParts} Parts)
            </span>
            <Loader2 className="w-4 h-4 text-emerald-400 animate-spin" />
          </div>

          {/* Table Container */}
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-[#221e33] text-xs font-semibold text-gray-500 bg-[#13111c]/30">
                  <th className="py-3 px-4">Part no.</th>
                  <th className="py-3 px-4">Serial no.</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Test Value</th>
                  <th className="py-3 px-4">Timestamp</th>
                  <th className="py-3 px-4">Attempt</th>
                  <th className="py-3 px-4">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1b172a] text-xs sm:text-sm font-medium text-gray-300">
                {data.failures.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-10 text-center text-gray-500 font-medium bg-[#13111c]/10">
                      <div className="flex flex-col items-center justify-center space-y-2">
                        <CheckCircle2 className="w-8 h-8 text-emerald-500" />
                        <span className="text-sm text-gray-400 font-semibold">
                          No leakage inspection failures recorded for this batch
                        </span>
                        <span className="text-xs text-gray-600">
                          All completed items from data embossing passed quality inspection.
                        </span>
                      </div>
                    </td>
                  </tr>
                ) : (
                  data.failures.map((item, idx) => (
                    <tr key={item.id ?? idx} className="hover:bg-[#151221]/30 transition-colors">
                      <td className="py-3.5 px-4 font-semibold text-white">
                        {item.partNo}
                      </td>
                      <td className="py-3.5 px-4 text-gray-400">
                        {item.serialNo}
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="bg-[#271012] text-red-400 border border-[#ef4444]/20 px-2.5 py-1 rounded-md text-xs font-semibold">
                          {item.status}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 font-bold text-red-400 flex items-center space-x-1">
                        <span>{typeof item.testValue === 'number' ? item.testValue.toFixed(2) : item.testValue}</span>
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
                      <td className="py-3.5 px-4">
                        <button
                          onClick={() => handleActionToggle(item)}
                          className={`font-bold px-2.5 py-1 rounded-md transition-colors text-xs border ${item.action === 'Scrap'
                            ? 'bg-[#271012] text-red-400 border-[#ef4444]/20 hover:bg-[#3d1317]'
                            : 'bg-[#20150b] text-[#f59e0b] border-[#f59e0b]/20 hover:bg-[#301b0c]'
                            }`}
                          title="Click to toggle action"
                        >
                          {item.action}
                        </button>
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
  );
};

export default LeakageTestingView;
