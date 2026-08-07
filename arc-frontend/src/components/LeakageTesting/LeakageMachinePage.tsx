import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Play,
  Pause,
  RotateCcw,
  Activity,
  Gauge,
  Sliders,
  TrendingUp,
  AlertCircle,
  CheckCircle,
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
} from 'recharts';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../../config/api';

interface MachineState {
  machineStatus: string;
  activeBatch: string;
  fileName: string;
  warningThreshold: number;
  alarmThreshold: number;
  unit: string;
  totalEmbossed: number;
  totalTested: number;
  passedParts: number;
  failedParts: number;
  progressPercent: number;
  activeChamber?: LiveChamber;
  trendData?: TrendPoint[];
  queue?: QueueItem[];
  history?: TestedRecord[];
}

interface LiveChamber {
  batchId: string;
  partNumber: string;
  serialNumber: string;
  currentPressure: number | null;
  unit: string;
  warningThreshold: number;
  alarmThreshold: number;
  status: string;
  cycleTimeSeconds: number;
  timestamp: string;
}

interface TrendPoint {
  serialNumber: string;
  partNumber: string;
  pressureValue: number;
  passed: boolean;
  timestamp: string;
}

interface QueueItem {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  status: string;
}

interface TestedRecord {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  pressureValue: number;
  unit: string;
  status: string;
  timestamp: string;
}

export const LeakageMachinePage: React.FC = () => {
  const [state, setState] = useState<MachineState>({
    machineStatus: 'IDLE',
    activeBatch: 'No Active Batch',
    fileName: 'Batch.csv',
    warningThreshold: 75.0,
    alarmThreshold: 80.0,
    unit: 'kPa',
    totalEmbossed: 0,
    totalTested: 0,
    passedParts: 0,
    failedParts: 0,
    progressPercent: 0,
    trendData: [],
    queue: [],
    history: [],
  });

  const [loading, setLoading] = useState<boolean>(true);
  const [testSpeed, setTestSpeed] = useState<number>(2000);

  // Fetch initial machine state from REST API
  const fetchMachineState = useCallback(async () => {
    try {
      const res = await fetch('/api/leakage-testing/machine/state');
      if (res.ok) {
        const data = await res.json();
        setState(data);
      }
    } catch (err) {
      console.error('Failed to fetch leakage machine state:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  // Connect STOMP WebSocket for real-time updates
  useEffect(() => {
    fetchMachineState();

    const endpoint = `${API_BASE_URL.replace(/\/$/, '')}/ws`;
    const client = new Client({
      webSocketFactory: () => new SockJS(endpoint) as unknown as WebSocket,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
      onConnect: () => {
        client.subscribe('/topic/leakage-testing', (message) => {
          try {
            const data: MachineState = JSON.parse(message.body);
            setState(data);
          } catch (e) {
            console.error('Error parsing leakage WS payload:', e);
          }
        });
      },
    });

    client.activate();
    const interval = setInterval(fetchMachineState, 4000);

    return () => {
      clearInterval(interval);
      void client.deactivate();
    };
  }, [fetchMachineState]);

  // Action controls
  const handleStart = async () => {
    try {
      const res = await fetch('/api/leakage-testing/machine/start', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        setState(data);
      }
    } catch (err) {
      console.error('Failed to start leakage testing:', err);
    }
  };

  const handlePause = async () => {
    try {
      const res = await fetch('/api/leakage-testing/machine/pause', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        setState(data);
      }
    } catch (err) {
      console.error('Failed to pause leakage testing:', err);
    }
  };

  const handleReset = async () => {
    try {
      const res = await fetch('/api/leakage-testing/machine/reset', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        setState(data);
      }
    } catch (err) {
      console.error('Failed to reset leakage testing:', err);
    }
  };

  const isTesting = state.machineStatus === 'TESTING';
  const chamber = state.activeChamber;
  const currentTestValue = chamber?.currentPressure ?? null;
  const warningThreshold = state.warningThreshold || 75.0;
  const alarmThreshold = state.alarmThreshold || 80.0;
  const queue = state.queue || [];
  const history = state.history || [];
  const trendData = state.trendData || [];

  return (
    <div className="space-y-6">
      {/* Top Details & Controls Bar */}
      <div className="flex flex-col xl:flex-row xl:items-center xl:justify-between gap-4 bg-[#0D0E19] border border-[#1b172a] p-6 rounded-3xl shadow-sm">
        <div className="space-y-2">
          <div className="flex items-center space-x-3">
            <span className="text-xs font-bold uppercase tracking-wider text-[#8b5cf6] bg-[#19122a] border border-[#3c1e6d] px-3 py-1 rounded-full">
              Leakage Machine
            </span>
            <span className="text-xs font-bold uppercase tracking-wider text-gray-400">
              Active Batch:
            </span>
            <span className="text-sm font-bold text-white border border-[#221e33] px-2.5 py-0.5 rounded-lg bg-[#13111c]">
              {state.activeBatch}
            </span>
          </div>
          <p className="text-xs text-[#8a8596]">
            Target threshold vacuum range extracted from <span className="font-semibold text-white">{state.fileName}</span>:
            <span className="ml-1 text-sm font-bold text-indigo-400 bg-indigo-950/40 px-2 py-0.5 rounded border border-indigo-900/60">
              {warningThreshold} – {alarmThreshold} {state.unit}
            </span>
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] p-1 rounded-xl">
            <button
              onClick={handleStart}
              disabled={isTesting || state.totalEmbossed === state.totalTested}
              className={`flex items-center space-x-1.5 px-4 py-2 rounded-lg font-bold text-xs transition-all duration-150 ${isTesting || state.totalEmbossed === state.totalTested
                ? 'text-gray-600 cursor-not-allowed'
                : 'bg-[#7c3aed] text-white hover:bg-[#6d28d9]'
                }`}
            >
              <Play className="w-3.5 h-3.5" />
              <span>Start</span>
            </button>
            <button
              onClick={handlePause}
              disabled={!isTesting}
              className={`flex items-center space-x-1.5 px-4 py-2 rounded-lg font-bold text-xs transition-all duration-150 ${!isTesting
                ? 'text-gray-600 cursor-not-allowed'
                : 'bg-amber-600/25 text-amber-450 border border-amber-500/25 hover:bg-amber-600/40'
                }`}
            >
              <Pause className="w-3.5 h-3.5" />
              <span>Pause</span>
            </button>
            <button
              onClick={handleReset}
              className="flex items-center space-x-1.5 px-3 py-2 rounded-lg font-bold text-xs text-gray-400 hover:bg-[#201d2d] transition-all"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset</span>
            </button>
          </div>

          {/* Speed Selector */}
          <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-3 py-2 rounded-xl text-xs font-semibold text-gray-400">
            <Sliders className="w-3.5 h-3.5 text-gray-500" />
            <span>Speed:</span>
            <select
              value={testSpeed}
              onChange={(e) => setTestSpeed(Number(e.target.value))}
              className="bg-transparent text-white font-bold outline-none border-none cursor-pointer"
            >
              <option value={1000} className="bg-[#13111c]">Fast (1s)</option>
              <option value={2000} className="bg-[#13111c]">Normal (2s)</option>
              <option value={4000} className="bg-[#13111c]">Slow (4s)</option>
            </select>
          </div>
        </div>
      </div>

      {/* KPI Counters Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* KPI 1 */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow duration-150">
          <span className="text-xs font-bold text-[#8a8596]">Total Embossed</span>
          <div className="text-3xl font-extrabold text-white mt-1">{state.totalEmbossed}</div>
          <div className="text-[10px] text-gray-500 mt-0.5">Ready for testing</div>
        </div>
        {/* KPI 2 */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow duration-150">
          <span className="text-xs font-bold text-[#8a8596]">Total Tested</span>
          <div className="text-3xl font-extrabold text-white mt-1">{state.totalTested}</div>
          <div className="text-[10px] text-gray-500 mt-0.5">Tested items</div>
        </div>
        {/* KPI 3 */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow duration-150 relative overflow-hidden">
          <span className="text-xs font-bold text-emerald-500">Passed Parts</span>
          <div className="text-3xl font-extrabold text-emerald-450 mt-1">{state.passedParts}</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Range: {warningThreshold} – {alarmThreshold} {state.unit}</div>
          <div className="absolute top-4 right-4 w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
        </div>
        {/* KPI 4 */}
        <div className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow duration-150 relative overflow-hidden">
          <span className="text-xs font-bold text-red-500">Failed Parts</span>
          <div className="text-3xl font-extrabold text-red-400 mt-1">{state.failedParts}</div>
          <div className="text-[10px] text-red-400/70 mt-0.5">Outside {warningThreshold} – {alarmThreshold} {state.unit}</div>
          {state.failedParts > 0 && (
            <div className="absolute top-4 right-4 w-2 h-2 rounded-full bg-red-500 animate-pulse" />
          )}
        </div>
      </div>

      {/* Live Testing Monitor Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Visual Inspection Panel */}
        <div className="lg:col-span-1 bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 flex flex-col justify-between min-h-[350px] shadow-sm">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-bold uppercase tracking-wider text-white">
                Live Chamber
              </h2>
              <Activity className={`w-4 h-4 ${isTesting ? 'text-indigo-400 animate-pulse' : 'text-gray-600'}`} />
            </div>

            {/* Chamber Box Graphic */}
            <div className={`relative border-2 rounded-2xl p-5 text-center flex flex-col items-center justify-center transition-all duration-300 min-h-[180px] bg-[#07050a] ${isTesting
              ? 'border-[#7c3aed] shadow-[0_0_20px_rgba(124,58,237,0.15)] animate-pulse'
              : 'border-[#221e33]'
              }`}>
              {chamber && chamber.partNumber !== '-' ? (
                <div className="space-y-3 w-full">
                  <div className="space-y-1">
                    <div className="text-xs font-semibold text-[#8a8596]">Testing Part</div>
                    <div className="text-sm font-bold text-white font-mono">{chamber.partNumber}</div>
                    <div className="text-xs text-gray-500 font-mono">SN: {chamber.serialNumber}</div>
                  </div>

                  {/* Realtime leakage indicator bar */}
                  <div className="space-y-1">
                    <div className="flex justify-between text-[10px] font-bold text-gray-400">
                      <span>Measured:</span>
                      <span className={currentTestValue !== null && (currentTestValue < warningThreshold || currentTestValue > alarmThreshold) ? 'text-red-400 font-extrabold' : 'text-emerald-400 font-bold'}>
                        {currentTestValue !== null ? `${currentTestValue.toFixed(1)} ${state.unit}` : 'Measuring...'}
                      </span>
                    </div>
                    <div className="w-full bg-[#13111c] h-3 rounded-full overflow-hidden border border-[#221e33] relative">
                      <div
                        className={`h-full transition-all duration-200 ${currentTestValue !== null && (currentTestValue < warningThreshold || currentTestValue > alarmThreshold) ? 'bg-red-500' : 'bg-emerald-500'
                          }`}
                        style={{ width: `${Math.min(100, Math.max(0, (((currentTestValue ?? warningThreshold) - 70) / (90 - 70)) * 100))}%` }}
                      />
                    </div>
                    <div className="flex justify-between text-[9px] text-gray-500 pt-0.5">
                      <span>Min: {warningThreshold} {state.unit}</span>
                      <span>Max: {alarmThreshold} {state.unit}</span>
                    </div>
                  </div>

                  {/* Visual Status Indicator Light */}
                  <div className="pt-2 flex justify-center">
                    {chamber.status === 'FAILED' ? (
                      <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full border border-red-500/20 bg-red-500/10 text-red-500 font-extrabold text-xs">
                        <AlertCircle className="w-3.5 h-3.5" />
                        <span>FAILED (Outside Threshold Range)</span>
                      </div>
                    ) : chamber.status === 'PASSED' ? (
                      <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full border border-emerald-500/20 bg-emerald-500/10 text-emerald-400 font-bold text-xs">
                        <CheckCircle className="w-3.5 h-3.5" />
                        <span>PASSED (Within Range)</span>
                      </div>
                    ) : (
                      <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full border border-yellow-500/20 bg-yellow-500/10 text-yellow-400 font-semibold text-xs">
                        <Activity className="w-3.5 h-3.5 animate-spin" />
                        <span>TESTING...</span>
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                <div className="text-gray-500 space-y-2">
                  <Gauge className="w-10 h-10 mx-auto text-gray-600" />
                  <div className="text-xs font-bold text-gray-400">
                    {state.totalEmbossed === state.totalTested && state.totalTested > 0
                      ? 'Testing Complete'
                      : isTesting
                        ? 'Waiting for Next Item...'
                        : 'Chamber Offline'}
                  </div>
                  <p className="text-[10px] text-gray-500 max-w-[180px] mx-auto">
                    {state.totalEmbossed === state.totalTested && state.totalTested > 0
                      ? 'All embossed jobs from Data Embossing have been tested.'
                      : 'Click Start to begin sequential testing.'}
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="text-[10px] text-gray-500">
            * Vacuum pressure decay test evaluates part seals. If leakage is outside {warningThreshold} – {alarmThreshold} {state.unit}, item is logged as Failed.
          </div>
        </div>

        {/* Graphical Representation Panel */}
        <div className="lg:col-span-2 bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 min-h-[350px] shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <TrendingUp className="w-4 h-4 text-[#8b5cf6]" />
              <h2 className="text-sm font-bold uppercase tracking-wider text-white">
                Leakage Trend per Part
              </h2>
            </div>
            <div className="text-[10px] font-semibold text-gray-500">
              Red dot indicates Failed Part outside range ({warningThreshold} – {alarmThreshold} {state.unit})
            </div>
          </div>

          {/* Chart Container */}
          <div className="w-full h-[240px]">
            {trendData.length === 0 ? (
              <div className="w-full h-full flex flex-col items-center justify-center border border-[#221e33] border-dashed rounded-2xl bg-[#07050a] text-gray-600">
                <TrendingUp className="w-10 h-10 text-gray-600 mb-2" />
                <span className="text-xs font-bold">No test trend data yet</span>
                <span className="text-[10px] text-gray-500 mt-0.5">Tested items will populate this live chart</span>
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid stroke="#1b172a" strokeDasharray="3 3" />
                  <XAxis
                    dataKey="serialNumber"
                    stroke="#8a8596"
                    fontSize={10}
                    tickLine={false}
                    tickFormatter={(val) => (val ? val.slice(-4) : '')}
                  />
                  <YAxis
                    stroke="#8a8596"
                    fontSize={10}
                    domain={[70, 90]}
                    tickCount={5}
                    tickLine={false}
                  />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0D0E19', border: '1px solid #1b172a', borderRadius: '10px' }}
                    labelStyle={{ color: '#fff', fontFamily: 'monospace', fontSize: 11 }}
                    itemStyle={{ color: '#8b5cf6', fontSize: 12 }}
                    formatter={(value: any) => [`${value} ${state.unit}`, 'Vacuum Pressure']}
                  />
                  <ReferenceLine
                    y={warningThreshold}
                    stroke="#10b981"
                    strokeDasharray="4 4"
                    label={{
                      value: `Min: ${warningThreshold}`,
                      fill: '#10b981',
                      fontSize: 10,
                      position: 'top',
                    }}
                  />
                  <ReferenceLine
                    y={alarmThreshold}
                    stroke="#ef4444"
                    strokeDasharray="4 4"
                    label={{
                      value: `Max: ${alarmThreshold}`,
                      fill: '#ef4444',
                      fontSize: 10,
                      position: 'top',
                    }}
                  />
                  <Line
                    type="monotone"
                    dataKey="pressureValue"
                    stroke="#8b5cf6"
                    strokeWidth={2}
                    activeDot={{ r: 6 }}
                    dot={(props: any) => {
                      const { cx, cy, payload } = props;
                      if (!payload) return null;
                      const isFailed = payload.pressureValue < warningThreshold || payload.pressureValue > alarmThreshold;
                      return (
                        <circle
                          key={props.key}
                          cx={cx}
                          cy={cy}
                          r={isFailed ? 5 : 3.5}
                          fill={isFailed ? '#ef4444' : '#10b981'}
                          stroke={isFailed ? '#ef4444' : '#10b981'}
                          strokeWidth={isFailed ? 2 : 1}
                        />
                      );
                    }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* Production Inspection Log Table */}
      <div className="bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 shadow-sm">
        <h2 className="text-base font-bold text-white mb-4 select-none">
          Leakage Inspection Results (Batch Status)
        </h2>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#221e33] text-xs font-semibold text-gray-500 bg-[#13111c]/30">
                <th className="py-3 px-4">Index</th>
                <th className="py-3 px-4">Part no.</th>
                <th className="py-3 px-4">Serial no.</th>
                <th className="py-3 px-4">Embossing</th>
                <th className="py-3 px-4">Inspection</th>
                <th className="py-3 px-4">Test Value</th>
                <th className="py-3 px-4">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1b172a] text-xs sm:text-sm font-semibold">
              {queue.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-10 text-center text-gray-500 font-medium">
                    No embossed parts found in active queue. Ensure active batch is embossed.
                  </td>
                </tr>
              ) : (
                queue.map((item, idx) => {
                  const testedRecord = history.find((h) => h.serialNumber === item.serialNumber);

                  let inspBadge = (
                    <span className="bg-[#19122a] text-[#8b5cf6] border border-[#3c1e6d] px-2 py-0.5 rounded text-[10px]">
                      Ready
                    </span>
                  );

                  if (testedRecord) {
                    inspBadge = equalsIgnoreCase('PASSED', testedRecord.status) ? (
                      <span className="bg-emerald-950/40 text-emerald-450 border border-emerald-900/40 px-2 py-0.5 rounded text-[10px]">
                        Pass
                      </span>
                    ) : (
                      <span className="bg-red-950/40 text-red-450 border border-red-900/40 px-2 py-0.5 rounded text-[10px]">
                        Fail
                      </span>
                    );
                  }

                  const val = testedRecord?.pressureValue;
                  const isValuePass = val !== undefined && val >= warningThreshold && val <= alarmThreshold;

                  return (
                    <tr
                      key={item.id}
                      className="hover:bg-[#151221]/30 transition-colors"
                    >
                      <td className="py-3 px-4 text-gray-500">{idx + 1}</td>
                      <td className="py-3 px-4 text-white font-mono">{item.partNumber}</td>
                      <td className="py-3 px-4 text-white font-mono">{item.serialNumber}</td>
                      <td className="py-3 px-4">
                        <span className="text-[10px] px-2 py-0.5 rounded bg-blue-950/40 text-blue-400 border border-blue-900/40">
                          COMPLETED
                        </span>
                      </td>
                      <td className="py-3 px-4">{inspBadge}</td>
                      <td className="py-3 px-4 font-mono font-bold">
                        {testedRecord ? (
                          <span className={isValuePass ? 'text-emerald-450' : 'text-red-400'}>
                            {testedRecord.pressureValue.toFixed(1)} {state.unit}
                          </span>
                        ) : (
                          <span className="text-gray-550">-</span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-gray-500">
                        {testedRecord?.timestamp || '-'}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

// Helper for case-insensitive string check
function equalsIgnoreCase(a?: string, b?: string) {
  return a?.toLowerCase() === b?.toLowerCase();
}

export default LeakageMachinePage;
