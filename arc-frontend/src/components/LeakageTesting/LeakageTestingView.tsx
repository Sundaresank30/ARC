import React, { useEffect, useState, useCallback } from 'react';
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
  CheckCircle,
} from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../../config/api';

export interface LeakageTestRecord {
  id?: number;
  partNo: string;
  serialNo: string;
  status: 'Failed' | 'Passed' | string;
  testValue: number;
  direction: 'up' | 'down' | string;
  timestamp: string;
  attempt: string;
  action: 'Scrap' | 'Pending' | 'Passed' | string;
}

export interface LeakageTestingDashboardData {
  activeBatch: string;
  failedCount: number;
  passedCount?: number;
  batchProgressPercent: number;
  completedCount: number;
  totalParts: number;
  dateDisplay: string;
  batchStatus?: string;
  failures: LeakageTestRecord[];
  passed?: LeakageTestRecord[];
}

export const LeakageTestingView: React.FC = () => {
  const [data, setData] = useState<LeakageTestingDashboardData>({
    activeBatch: 'Batch_1',
    failedCount: 0,
    passedCount: 0,
    batchProgressPercent: 100,
    completedCount: 100,
    totalParts: 100,
    dateDisplay: '06 August, 2026',
    batchStatus: '100% completed',
    failures: [],
    passed: [],
  });
  const [loading, setLoading] = useState<boolean>(true);

  const fetchDashboardData = useCallback(async () => {
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
  }, []);

  // Set up WebSocket real-time listener & REST polling
  useEffect(() => {
    fetchDashboardData();

    const endpoint = `${API_BASE_URL.replace(/\/$/, '')}/ws`;
    const client = new Client({
      webSocketFactory: () => new SockJS(endpoint) as unknown as WebSocket,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
      onConnect: () => {
        client.subscribe('/topic/leakage-testing', () => {
          fetchDashboardData();
        });
        client.subscribe('/topic/leakage-progress', () => {
          fetchDashboardData();
        });
      },
    });

    client.activate();
    const interval = setInterval(fetchDashboardData, 3000);

    return () => {
      clearInterval(interval);
      void client.deactivate();
    };
  }, [fetchDashboardData]);

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

  const isBatchCompleted = data.batchProgressPercent >= 100 || data.batchStatus === '100% completed';
  const passedResults = data.passed || [];
  const failureResults = data.failures || [];

  // Helper render component for Green Batch Progress Banner Pill
  const renderBatchProgressBar = () => (
    <div className="bg-[#0a231b] border border-emerald-950/40 rounded-xl px-4 py-3 flex items-center justify-between my-2">
      <div className="flex items-center space-x-2">
        {isBatchCompleted ? (
          <CheckCircle2 className="w-5 h-5 text-emerald-400" />
        ) : (
          <Loader2 className="w-4 h-4 text-emerald-400 animate-spin" />
        )}
        <span className="text-xs sm:text-sm font-semibold text-emerald-400">
          Batch Progress: {data.batchProgressPercent}% Complete ({data.completedCount} / {data.totalParts} Parts)
        </span>
      </div>

      <span className={`text-xs font-extrabold px-3 py-1 rounded-md border ${isBatchCompleted
        ? 'bg-emerald-900/40 text-emerald-300 border-emerald-700/50'
        : 'bg-indigo-900/40 text-indigo-300 border-indigo-700/50'
        }`}>
        {isBatchCompleted ? '100% completed' : 'In Progress'}
      </span>
    </div>
  );

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Workspace Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-white tracking-tight leading-tight">
            Leakage Testing
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Review automated leakage inspection test results and monitor quality actions in real-time
          </p>
        </div>

        {/* Date Badge - Real-time Current Day */}
        <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
          <Calendar className="w-4 h-4 text-emerald-400" />
          <span className="text-sm font-semibold text-gray-200">{data.dateDisplay}</span>
        </div>
      </div>

      {/* Overview Cards Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Active Batch Card */}
        <div className="md:col-span-2 bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow duration-150">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-semibold text-white">
              Active Batch
            </h2>
            <span className={`text-xs font-bold px-3 py-1 rounded-full border ${isBatchCompleted
              ? 'bg-emerald-950/60 text-emerald-400 border-emerald-800/60'
              : 'bg-indigo-950/60 text-indigo-400 border-indigo-800/60'
              }`}>
              {isBatchCompleted ? '100% completed' : 'In Progress'}
            </span>
          </div>

          <div className="flex items-center space-x-4 py-2">
            {/* Batch Item */}
            <div className="flex items-center space-x-2 text-emerald-400 bg-[#0a231b] px-3 py-1.5 rounded-lg border border-emerald-950/40 font-mono">
              <FileText className="w-4 h-4 text-emerald-400" />
              <span className="text-sm font-bold">{data.activeBatch}</span>
            </div>

            {/* Stepper Arrow Line */}
            <div className="flex-1 flex items-center justify-center relative">
              <div className="w-full border-t border-[#1b172a] flex items-center justify-center">
                <ArrowRight className="w-4 h-4 text-gray-500 absolute bg-[#0D0E19] px-0.5" />
              </div>
            </div>

            {/* Step Target */}
            <div className="flex items-center space-x-2 text-gray-500 bg-[#13111c] px-3 py-1.5 rounded-lg border border-[#221e33]">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span className="text-sm font-semibold text-gray-300">Leakage Testing</span>
            </div>
          </div>
        </div>

        {/* Failed KPI Card (Monitored in real-time) */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow duration-150">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-[15px] text-gray-300">Failed</span>
            <div className="w-8 h-8 rounded-lg bg-[#271012] border border-[#ef4444]/20 flex items-center justify-center text-red-500">
              <AlertCircle className="w-4.5 h-4.5" />
            </div>
          </div>

          <div className="my-1">
            <span className="text-[44px] font-bold leading-none tracking-tight text-red-400">
              {data.failedCount}
            </span>
          </div>

          <div>
            <span className="text-xs font-semibold text-red-500">-from active batch ({data.activeBatch})</span>
          </div>
        </div>
      </div>

      {/* Main Leakage Inspection Results Card */}
      <div className="bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 sm:p-8 shadow-sm">
        {/* Card Section Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-2">
            <h2 className="text-xl font-bold text-white tracking-tight">
              Leakage Inspection Results
            </h2>
            <Info className="w-4 h-4 text-gray-500" />
          </div>
          <span className="text-xs font-medium text-gray-400">
            Batch ID: <span className="text-white font-semibold font-mono">{data.activeBatch}</span>
          </span>
        </div>

        {/* Inner Content Area */}
        <div className="border border-[#1b172a] rounded-2xl overflow-hidden shadow-sm space-y-6 p-4 sm:p-6 bg-[#0D0E19]">

          {/* ----------------------------------------------------------------- */}
          {/* TABLE 1: LEAKED TESTING FAILURES */}
          {/* ----------------------------------------------------------------- */}
          <div className="space-y-3">
            {/* Header Alert */}
            <div className="bg-[#271012] border border-[#ef4444]/25 rounded-xl p-3.5 sm:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center space-x-2 text-red-400 font-bold text-sm sm:text-base">
                <Clock className="w-4 h-4 text-red-500" />
                <span>Leaked Testing Failures ({failureResults.length})</span>
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

            {/* Batch Progress Banner directly below Leakage Testing Failures */}
            {renderBatchProgressBar()}

            {/* Table Container */}
            <div className="overflow-x-auto rounded-xl border border-[#271012]">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#221e33] text-xs font-semibold text-gray-400 bg-[#170c10]">
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
                  {failureResults.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="py-8 text-center text-gray-500 font-medium bg-[#13111c]/10">
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
                      <tr key={item.id ?? idx} className="hover:bg-[#151221]/30 transition-colors">
                        <td className="py-3 px-4 font-semibold text-white font-mono">
                          {item.partNo}
                        </td>
                        <td className="py-3 px-4 text-gray-400 font-mono">
                          {item.serialNo}
                        </td>
                        <td className="py-3 px-4">
                          <span className="bg-[#271012] text-red-400 border border-[#ef4444]/20 px-2.5 py-1 rounded-md text-xs font-semibold">
                            {item.status}
                          </span>
                        </td>
                        <td className="py-3 px-4 font-bold text-red-400 flex items-center space-x-1 font-mono">
                          <span>{typeof item.testValue === 'number' ? item.testValue.toFixed(2) : item.testValue} kPa</span>
                          {item.direction === 'down' ? (
                            <ArrowDown className="w-3.5 h-3.5 text-red-500" />
                          ) : (
                            <ArrowUp className="w-3.5 h-3.5 text-red-500" />
                          )}
                        </td>
                        <td className="py-3 px-4 text-gray-500">
                          {item.timestamp}
                        </td>
                        <td className="py-3 px-4 text-gray-500">
                          {item.attempt}
                        </td>
                        <td className="py-3 px-4">
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

          {/* ----------------------------------------------------------------- */}
          {/* TABLE 2: PASSED INSPECTION RESULTS */}
          {/* ----------------------------------------------------------------- */}
          <div className="space-y-3 pt-4 border-t border-[#1b172a]">
            {/* Header Success Banner */}
            <div className="bg-[#0a231b] border border-emerald-900/40 rounded-xl p-3.5 sm:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center space-x-2 text-emerald-400 font-bold text-sm sm:text-base">
                <CheckCircle className="w-4 h-4 text-emerald-400" />
                <span>Passed Inspection Results ({passedResults.length})</span>
              </div>

              <div className="flex items-center flex-wrap gap-2 text-xs font-semibold">
                <span className="text-emerald-400 bg-emerald-950/60 px-3 py-1 rounded-md border border-emerald-900/40">
                  Quality Approved
                </span>
                <span className="text-emerald-400 bg-emerald-950/60 px-3 py-1 rounded-md border border-emerald-900/40">
                  Within 75.0 – 80.0 kPa
                </span>
              </div>
            </div>

            {/* Batch Progress Banner directly below Passed Inspection Result */}
            {renderBatchProgressBar()}

            {/* Table Container */}
            <div className="overflow-x-auto rounded-xl border border-emerald-950/40">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#221e33] text-xs font-semibold text-gray-400 bg-[#091a14]">
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
                  {passedResults.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="py-8 text-center text-gray-500 font-medium bg-[#13111c]/10">
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
                      <tr key={item.id ?? idx} className="hover:bg-[#151221]/30 transition-colors">
                        <td className="py-3 px-4 font-semibold text-white font-mono">
                          {item.partNo}
                        </td>
                        <td className="py-3 px-4 text-gray-400 font-mono">
                          {item.serialNo}
                        </td>
                        <td className="py-3 px-4">
                          <span className="bg-emerald-950/60 text-emerald-400 border border-emerald-900/40 px-2.5 py-1 rounded-md text-xs font-semibold">
                            {item.status}
                          </span>
                        </td>
                        <td className="py-3 px-4 font-bold text-emerald-400 flex items-center space-x-1 font-mono">
                          <span>{typeof item.testValue === 'number' ? item.testValue.toFixed(2) : item.testValue} kPa</span>
                          <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
                        </td>
                        <td className="py-3 px-4 text-gray-500">
                          {item.timestamp}
                        </td>
                        <td className="py-3 px-4 text-gray-500">
                          {item.attempt}
                        </td>
                        <td className="py-3 px-4">
                          <span className="font-semibold text-xs text-emerald-400 bg-emerald-950/40 px-2.5 py-1 rounded-md border border-emerald-900/30">
                            {item.action || 'Passed'}
                          </span>
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
