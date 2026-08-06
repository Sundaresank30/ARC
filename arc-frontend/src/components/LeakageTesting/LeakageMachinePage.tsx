import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Play,
  Pause,
  RotateCcw,
  Calendar,
  FileText,
  ShieldCheck,
  AlertCircle,
  CheckCircle,
  Activity,
  Flame,
  Gauge,
  Sliders,
  ChevronRight,
  TrendingUp,
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

// Mock mapping of file names and threshold values for data integration
const BATCH_FILE_MAPPING: Record<string, { fileName: string; threshold: number }> = {
  'Batch_1': { fileName: 'Batch_1.csv', threshold: 0.50 },
  'Batch_2': { fileName: 'Batch_2.csv', threshold: 0.75 },
  'Batch_3': { fileName: 'Batch_3.csv', threshold: 0.60 },
};

interface EmbossingJob {
  id: number;
  batchId: string;
  partNumber: string;
  serialNumber: string;
  embossingStatus: 'PENDING' | 'IN_MACHINE' | 'PRINTING' | 'COMPLETED' | 'FAILED';
  testValue: number | null;
  direction: string | null;
  attempt: string | null;
  action: string | null;
}

interface TestedState {
  testValue: number;
  passed: boolean;
  timestamp: string;
}

export const LeakageMachinePage: React.FC = () => {
  const [activeBatch, setActiveBatch] = useState<string>('Batch_1');
  const [threshold, setThreshold] = useState<number>(0.50);
  const [fileName, setFileName] = useState<string>('Batch_1.csv');
  const [jobs, setJobs] = useState<EmbossingJob[]>([]);
  const [testedMap, setTestedMap] = useState<Record<string, TestedState>>({});
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [currentIndex, setCurrentIndex] = useState<number>(-1);
  const [testSpeed, setTestSpeed] = useState<number>(2000); // ms per part
  const [currentTestValue, setCurrentTestValue] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const loopTimeoutRef = useRef<number | null>(null);
  const isRunningRef = useRef<boolean>(false);
  const currentIndexRef = useRef<number>(-1);

  // Sync refs for the simulation loop
  useEffect(() => {
    isRunningRef.current = isRunning;
  }, [isRunning]);

  useEffect(() => {
    currentIndexRef.current = currentIndex;
  }, [currentIndex]);

  // Load tested results map from local storage for persistence
  const loadTestedMap = (batchId: string) => {
    try {
      const stored = localStorage.getItem(`leakage_tested_${batchId}`);
      if (stored) {
        setTestedMap(JSON.parse(stored));
      } else {
        setTestedMap({});
      }
    } catch (e) {
      console.error('Failed to load local storage tested map:', e);
      setTestedMap({});
    }
  };

  const saveTestedMap = (batchId: string, map: Record<string, TestedState>) => {
    try {
      localStorage.setItem(`leakage_tested_${batchId}`, JSON.stringify(map));
    } catch (e) {
      console.error('Failed to save tested map to local storage:', e);
    }
  };

  // Fetch batches & jobs from Backend Embossing dashboard
  const fetchJobsData = async (shouldResetIndex = false) => {
    try {
      const res = await fetch('/api/embossing/dashboard');
      if (res.ok) {
        const dashboard = await res.json();
        const batchId = dashboard.activeBatch || 'Batch_1';
        setActiveBatch(batchId);

        // Consume threshold mapping
        const mapping = BATCH_FILE_MAPPING[batchId] || { fileName: `${batchId}.csv`, threshold: 0.50 };
        setThreshold(mapping.threshold);
        setFileName(mapping.fileName);

        // Fetch completed jobs from embossing backend
        const allJobs: EmbossingJob[] = dashboard.jobs || [];
        setJobs(allJobs);

        // Load tested states
        loadTestedMap(batchId);

        if (shouldResetIndex) {
          setCurrentIndex(-1);
          setCurrentTestValue(null);
        }
      }
    } catch (err) {
      console.error('Failed to fetch embossing dashboard jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobsData(true);
    const interval = setInterval(() => {
      if (!isRunningRef.current) {
        fetchJobsData(false);
      }
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  // Post failure status to backend DB for integration
  const notifyBackendFailure = async (jobId: number, val: number) => {
    try {
      const direction = val > 1.0 ? 'up' : 'down';
      await fetch(`/api/leakage-testing/jobs/${jobId}/fail?testValue=${val}&direction=${direction}&attempt=1/2&action=Pending`, {
        method: 'POST',
      });
    } catch (err) {
      console.error('Failed to report leakage failure to backend:', err);
    }
  };

  // Run sequential simulation test loop
  const runSimulationStep = useCallback(async () => {
    if (!isRunningRef.current) return;

    // Find completed embossed parts that haven't been tested yet
    const readyJobs = jobs.filter(
      (job) =>
        (job.embossingStatus === 'COMPLETED' || job.embossingStatus === 'FAILED') &&
        !testedMap[job.id]
    );

    if (readyJobs.length === 0) {
      // No parts ready to test, pause simulation
      setIsRunning(false);
      setCurrentTestValue(null);
      return;
    }

    // Pick the first ready job in sequence
    const nextJob = readyJobs[0];

    // Find its absolute index in jobs list
    const jobIndex = jobs.findIndex((j) => j.id === nextJob.id);
    setCurrentIndex(jobIndex);

    // Simulate tested value
    // Make it realistic: most parts pass, some parts fail (exceed threshold)
    const seed = Math.random();
    let simulatedValue = 0.2 + Math.random() * 0.25; // 0.20 - 0.45 (Pass)
    if (seed > 0.85) {
      // Exceeds threshold (Fail)
      simulatedValue = threshold + 0.05 + Math.random() * 0.4; // Exceed threshold
    }
    // Round to 3 decimal places
    simulatedValue = Math.round(simulatedValue * 1000) / 1000;

    setCurrentTestValue(simulatedValue);

    const passed = simulatedValue <= threshold;
    const timeStr = new Date().toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });

    const newTestedState: TestedState = {
      testValue: simulatedValue,
      passed,
      timestamp: timeStr,
    };

    // Update locally and persist in local storage
    const updatedMap = {
      ...testedMap,
      [nextJob.id]: newTestedState,
    };
    setTestedMap(updatedMap);
    saveTestedMap(activeBatch, updatedMap);

    // If it fails, report it to the backend database to keep state synchronized
    if (!passed) {
      await notifyBackendFailure(nextJob.id, simulatedValue);
    }

    // Trigger next step after delay
    if (isRunningRef.current) {
      loopTimeoutRef.current = window.setTimeout(runSimulationStep, testSpeed);
    }
  }, [jobs, testedMap, threshold, testSpeed, activeBatch]);

  // Handle Play/Pause
  useEffect(() => {
    if (isRunning) {
      loopTimeoutRef.current = window.setTimeout(runSimulationStep, 200);
    } else {
      if (loopTimeoutRef.current) {
        clearTimeout(loopTimeoutRef.current);
      }
    }
    return () => {
      if (loopTimeoutRef.current) clearTimeout(loopTimeoutRef.current);
    };
  }, [isRunning, runSimulationStep]);

  const handleReset = () => {
    setIsRunning(false);
    if (loopTimeoutRef.current) clearTimeout(loopTimeoutRef.current);
    localStorage.removeItem(`leakage_tested_${activeBatch}`);
    setTestedMap({});
    setCurrentIndex(-1);
    setCurrentTestValue(null);
    fetchJobsData(true);
  };

  // Derive counts
  const totalEmbossed = jobs.filter(
    (j) => j.embossingStatus === 'COMPLETED' || j.embossingStatus === 'FAILED'
  ).length;
  const testedCount = Object.keys(testedMap).length;
  const passedCount = Object.values(testedMap).filter((t) => t.passed).length;
  const failedCount = Object.values(testedMap).filter((t) => !t.passed).length;

  // Prepare chart data
  const chartData = jobs
    .filter((job) => testedMap[job.id])
    .map((job) => ({
      serialNumber: job.serialNumber,
      testValue: testedMap[job.id]?.testValue,
      passed: testedMap[job.id]?.passed,
    }));

  const currentJob = currentIndex >= 0 && currentIndex < jobs.length ? jobs[currentIndex] : null;

  return (
    <div className="space-y-6">
      {/* Top Details & Controls Bar */}
      <div className="flex flex-col xl:flex-row xl:items-center xl:justify-between gap-4 bg-[#0D0E19] border border-[#1b172a] p-6 rounded-3xl">
        <div className="space-y-2">
          <div className="flex items-center space-x-3">
            <span className="text-xs font-bold uppercase tracking-wider text-[#8b5cf6] bg-[#19122a] border border-[#3c1e6d] px-3 py-1 rounded-full">
              Leakage Machine
            </span>
            <span className="text-xs font-bold uppercase tracking-wider text-gray-400">
              Active Batch:
            </span>
            <span className="text-sm font-bold text-white border border-[#221e33] px-2.5 py-0.5 rounded-lg bg-[#13111c]">
              {activeBatch}
            </span>
          </div>
          <p className="text-xs text-[#8a8596]">
            Target threshold value extracted from <span className="font-semibold text-white">{fileName}</span>:
            <span className="ml-1 text-sm font-bold text-indigo-400 bg-indigo-950/40 px-2 py-0.5 rounded border border-indigo-900/60">{threshold} sccm</span>
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] p-1 rounded-xl">
            <button
              onClick={() => setIsRunning(true)}
              disabled={isRunning || totalEmbossed === testedCount}
              className={`flex items-center space-x-1.5 px-4 py-2 rounded-lg font-bold text-xs transition-all duration-150 ${isRunning || totalEmbossed === testedCount
                ? 'text-gray-600 cursor-not-allowed'
                : 'bg-[#7c3aed] text-white hover:bg-[#6d28d9]'
                }`}
            >
              <Play className="w-3.5 h-3.5" />
              <span>Start</span>
            </button>
            <button
              onClick={() => setIsRunning(false)}
              disabled={!isRunning}
              className={`flex items-center space-x-1.5 px-4 py-2 rounded-lg font-bold text-xs transition-all duration-150 ${!isRunning
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
        <div className="bg-[#0D0E19] border border-[#1b172a] rounded-2xl p-5 shadow-sm">
          <span className="text-xs font-bold text-[#8a8596]">Total Embossed</span>
          <div className="text-3xl font-extrabold text-white mt-1">{totalEmbossed}</div>
          <div className="text-[10px] text-gray-500 mt-0.5">Ready for testing</div>
        </div>
        {/* KPI 2 */}
        <div className="bg-[#0D0E19] border border-[#1b172a] rounded-2xl p-5 shadow-sm">
          <span className="text-xs font-bold text-[#8a8596]">Total Tested</span>
          <div className="text-3xl font-extrabold text-white mt-1">{testedCount}</div>
          <div className="text-[10px] text-gray-500 mt-0.5">Tested items</div>
        </div>
        {/* KPI 3 */}
        <div className="bg-[#0D0E19] border border-emerald-950/40 rounded-2xl p-5 shadow-sm relative overflow-hidden">
          <span className="text-xs font-bold text-emerald-500">Passed Parts</span>
          <div className="text-3xl font-extrabold text-emerald-400 mt-1">{passedCount}</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Leakage ≤ {threshold}</div>
          <div className="absolute top-4 right-4 w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
        </div>
        {/* KPI 4 */}
        <div className="bg-[#0D0E19] border border-red-950/40 rounded-2xl p-5 shadow-sm relative overflow-hidden">
          <span className="text-xs font-bold text-red-500">Failed Parts</span>
          <div className="text-3xl font-extrabold text-red-400 mt-1">{failedCount}</div>
          <div className="text-[10px] text-red-650 mt-0.5">Exceeds {threshold} sccm</div>
          {failedCount > 0 && (
            <div className="absolute top-4 right-4 w-2 h-2 rounded-full bg-red-500 animate-pulse" />
          )}
        </div>
      </div>

      {/* Live Testing Monitor Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Visual Inspection Panel */}
        <div className="lg:col-span-1 bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 flex flex-col justify-between min-h-[350px]">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-bold uppercase tracking-wider text-white">
                Live Chamber
              </h2>
              <Activity className={`w-4 h-4 ${isRunning ? 'text-indigo-400 animate-pulse' : 'text-gray-600'}`} />
            </div>

            {/* Chamber Box Graphic */}
            <div className={`relative border-2 rounded-2xl p-5 text-center flex flex-col items-center justify-center transition-all duration-300 min-h-[180px] bg-[#07050a] ${isRunning
              ? 'border-[#7c3aed] shadow-[0_0_20px_rgba(124,58,237,0.15)] animate-pulse'
              : 'border-[#221e33]'
              }`}>
              {currentJob ? (
                <div className="space-y-3 w-full">
                  <div className="space-y-1">
                    <div className="text-xs font-semibold text-[#8a8596]">Testing Part</div>
                    <div className="text-sm font-bold text-white font-mono">{currentJob.partNumber}</div>
                    <div className="text-xs text-gray-500 font-mono">SN: {currentJob.serialNumber}</div>
                  </div>

                  {/* Realtime leakage indicator bar */}
                  <div className="space-y-1">
                    <div className="flex justify-between text-[10px] font-bold text-gray-400">
                      <span>Value:</span>
                      <span className={currentTestValue && currentTestValue > threshold ? 'text-red-400 font-extrabold' : 'text-emerald-400 font-bold'}>
                        {currentTestValue !== null ? `${currentTestValue.toFixed(3)} sccm` : 'Measuring...'}
                      </span>
                    </div>
                    <div className="w-full bg-[#13111c] h-3 rounded-full overflow-hidden border border-[#221e33] relative">
                      <div
                        className={`h-full transition-all duration-200 ${currentTestValue && currentTestValue > threshold ? 'bg-red-500' : 'bg-emerald-500'
                          }`}
                        style={{ width: `${Math.min(100, (currentTestValue || 0) / 1.2 * 100)}%` }}
                      />
                      {/* Threshold marker */}
                      <div
                        className="absolute top-0 bottom-0 w-0.5 bg-red-600/70"
                        style={{ left: `${(threshold / 1.2) * 100}%` }}
                      />
                    </div>
                  </div>

                  {/* Visual Status Indicator Light */}
                  <div className="pt-2 flex justify-center">
                    {currentTestValue !== null ? (
                      currentTestValue > threshold ? (
                        <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full border border-red-500/20 bg-red-500/10 text-red-500 font-extrabold text-xs">
                          <AlertCircle className="w-3.5 h-3.5" />
                          <span>FAILED (Exceeded Threshold)</span>
                        </div>
                      ) : (
                        <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full border border-emerald-500/20 bg-emerald-500/10 text-emerald-400 font-bold text-xs">
                          <CheckCircle className="w-3.5 h-3.5" />
                          <span>PASSED</span>
                        </div>
                      )
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
                    {totalEmbossed === testedCount && testedCount > 0
                      ? 'Testing Complete'
                      : isRunning
                        ? 'Waiting for Next Item...'
                        : 'Chamber Offline'}
                  </div>
                  <p className="text-[10px] text-gray-500 max-w-[180px] mx-auto">
                    {totalEmbossed === testedCount && testedCount > 0
                      ? 'All embossed jobs from Data Embossing have been tested.'
                      : 'Click Start to begin sequential testing.'}
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="text-[10px] text-gray-500">
            * Pressure decay test evaluates part seals. If leakage exceeds {threshold} sccm, item is logged as Failed.
          </div>
        </div>

        {/* Graphical Representation Panel */}
        <div className="lg:col-span-2 bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6 min-h-[350px]">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <TrendingUp className="w-4 h-4 text-[#8b5cf6]" />
              <h2 className="text-sm font-bold uppercase tracking-wider text-white">
                Leakage Trend per Part
              </h2>
            </div>
            <div className="text-[10px] font-semibold text-gray-500">
              Red dot indicates Failed Part exceeding threshold ({threshold})
            </div>
          </div>

          {/* Chart Container */}
          <div className="w-full h-[240px]">
            {chartData.length === 0 ? (
              <div className="w-full h-full flex flex-col items-center justify-center border border-[#221e33] border-dashed rounded-2xl bg-[#07050a] text-gray-600">
                <TrendingUp className="w-10 h-10 text-gray-600 mb-2" />
                <span className="text-xs font-bold">No test trend data yet</span>
                <span className="text-[10px] text-gray-500 mt-0.5">Tested items will populate this live chart</span>
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid stroke="#1b172a" strokeDasharray="3 3" />
                  <XAxis
                    dataKey="serialNumber"
                    stroke="#8a8596"
                    fontSize={10}
                    tickLine={false}
                    tickFormatter={(val) => val.slice(-4)}
                  />
                  <YAxis
                    stroke="#8a8596"
                    fontSize={10}
                    domain={[0, 1.2]}
                    tickCount={5}
                    tickLine={false}
                  />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0D0E19', border: '1px solid #1b172a', borderRadius: '10px' }}
                    labelStyle={{ color: '#fff', fontFamily: 'monospace', fontSize: 11 }}
                    itemStyle={{ color: '#8b5cf6', fontSize: 12 }}
                    formatter={(value: any) => [`${value} sccm`, 'Leakage']}
                  />
                  {/* Threshold Line */}
                  <ReferenceLine
                    y={threshold}
                    stroke="#ef4444"
                    strokeDasharray="4 4"
                    label={{
                      value: `Threshold: ${threshold}`,
                      fill: '#ef4444',
                      fontSize: 10,
                      position: 'top',
                    }}
                  />
                  <Line
                    type="monotone"
                    dataKey="testValue"
                    stroke="#8b5cf6"
                    strokeWidth={2}
                    activeDot={{ r: 6 }}
                    dot={(props: any) => {
                      const { cx, cy, payload } = props;
                      if (!payload) return null;
                      const isFailed = payload.testValue > threshold;
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
      <div className="bg-[#0D0E19] border border-[#1b172a] rounded-3xl p-6">
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
              {jobs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-10 text-center text-gray-500 font-medium">
                    No batch items found. Ensure active batch is prepared.
                  </td>
                </tr>
              ) : (
                jobs.map((job, idx) => {
                  const testedState = testedMap[job.id];
                  const isEmbossed = job.embossingStatus === 'COMPLETED' || job.embossingStatus === 'FAILED';

                  // Inspection status badge details
                  let inspBadge = (
                    <span className="bg-gray-850 text-gray-400 border border-gray-700 px-2 py-0.5 rounded text-[10px]">
                      Waiting
                    </span>
                  );
                  if (testedState) {
                    inspBadge = testedState.passed ? (
                      <span className="bg-emerald-950/40 text-emerald-450 border border-emerald-900/40 px-2 py-0.5 rounded text-[10px]">
                        Pass
                      </span>
                    ) : (
                      <span className="bg-red-950/40 text-red-450 border border-red-900/40 px-2 py-0.5 rounded text-[10px]">
                        Fail
                      </span>
                    );
                  } else if (isEmbossed) {
                    inspBadge = (
                      <span className="bg-[#19122a] text-[#8b5cf6] border border-[#3c1e6d] px-2 py-0.5 rounded text-[10px] animate-pulse">
                        Ready
                      </span>
                    );
                  }

                  const val = testedState?.testValue;

                  return (
                    <tr
                      key={job.id}
                      className={`hover:bg-[#151221]/30 transition-colors ${currentIndex === idx ? 'bg-[#1e1430]/40' : ''
                        }`}
                    >
                      <td className="py-3 px-4 text-gray-500">{idx + 1}</td>
                      <td className="py-3 px-4 text-white font-mono">{job.partNumber}</td>
                      <td className="py-3 px-4 text-white font-mono">{job.serialNumber}</td>
                      <td className="py-3 px-4">
                        <span className={`text-[10px] px-2 py-0.5 rounded ${isEmbossed
                          ? 'bg-blue-950/40 text-blue-400 border border-blue-900/40'
                          : 'bg-[#181524] text-gray-500 border border-[#2c283d]'
                          }`}>
                          {job.embossingStatus}
                        </span>
                      </td>
                      <td className="py-3 px-4">{inspBadge}</td>
                      <td className="py-3 px-4 font-mono font-bold">
                        {val !== undefined ? (
                          <span className={val > threshold ? 'text-red-400' : 'text-emerald-450'}>
                            {val.toFixed(3)}
                          </span>
                        ) : (
                          <span className="text-gray-550">-</span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-gray-500">
                        {testedState?.timestamp || '-'}
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

export default LeakageMachinePage;
