import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  Play,
  Cpu,
  CheckCircle2,
  Clock,
  Zap,
  RefreshCw,
  Layers,
  CheckCheck,
  AlertCircle,
} from 'lucide-react';
import {
  EmbossingQueueRecord,
  getQueueBuffer,
  claimNextWaitingQueueItem,
  markQueueItemInProgress,
  markQueueItemCompleted,
} from '../../api/machine';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------
const BUFFER_SIZE = 5;
/** Duration of the existing print routine (kept unchanged). */
const PRINT_ROUTINE_MS = 1200;

function notifyEmbossingDataUpdated() {
  window.dispatchEvent(new Event('embossing-data-updated'));

  if ('BroadcastChannel' in window) {
    const channel = new BroadcastChannel('arc-embossing');
    channel.postMessage('data-updated');
    channel.close();
  }
}

// ---------------------------------------------------------------------------
// MachinePage
// ---------------------------------------------------------------------------
export const MachinePage: React.FC = () => {
  const [records, setRecords] = useState<EmbossingQueueRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeItem, setActiveItem] = useState<EmbossingQueueRecord | null>(null);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [lastCompletedItem, setLastCompletedItem] = useState<EmbossingQueueRecord | null>(null);

  // Prevent concurrent loop invocations and support pause/resume
  const loopRunningRef = useRef<boolean>(false);
  const stopRequestedRef = useRef<boolean>(false);
  const recordsRef = useRef<EmbossingQueueRecord[]>([]);
  const activeItemRef = useRef<EmbossingQueueRecord | null>(null);
  const printAbortControllerRef = useRef<AbortController | null>(null);

  // ---------------------------------------------------------------------------
  // Load buffer from DB on mount / refresh
  // ---------------------------------------------------------------------------
  const loadRecords = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getQueueBuffer();
      setRecords(data);
    } catch (error) {
      console.warn('Failed to load queue buffer from DB:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    recordsRef.current = records;
  }, [records]);

  useEffect(() => {
    activeItemRef.current = activeItem;
  }, [activeItem]);

  useEffect(() => {
    const interval = window.setInterval(() => {
      if (!isRunning) {
        loadRecords();
      }
    }, 5000);

    return () => window.clearInterval(interval);
  }, [isRunning, loadRecords]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  // ---------------------------------------------------------------------------
  // Derived helpers
  // ---------------------------------------------------------------------------
  const pausedItemFromBuffer = records.find((r) => r.status === 'IN_PROGRESS');
  const nextWaitingItem = records.find((r) => r.status === 'WAITING');
  const noDataAvailable = records.length === 0 && !activeItem;
  const allCompleted = noDataAvailable;
  const displayActiveItem = activeItem ?? pausedItemFromBuffer ?? null;

  // ---------------------------------------------------------------------------
  // Core print routine (UNCHANGED — same 1200ms delay as original)
  // ---------------------------------------------------------------------------
  const runPrintRoutine = useCallback(
    (_item: EmbossingQueueRecord): Promise<void> =>
      new Promise((resolve, reject) => {
        if (stopRequestedRef.current) {
          return reject(new Error('PAUSED'));
        }

        const abortController = new AbortController();
        printAbortControllerRef.current = abortController;

        const timer = window.setTimeout(() => {
          printAbortControllerRef.current = null;
          resolve();
        }, PRINT_ROUTINE_MS);

        abortController.signal.addEventListener('abort', () => {
          window.clearTimeout(timer);
          printAbortControllerRef.current = null;
          reject(new Error('PAUSED'));
        });
      }),
    []
  );

  const abortCurrentPrint = useCallback(() => {
    if (printAbortControllerRef.current) {
      printAbortControllerRef.current.abort();
      printAbortControllerRef.current = null;
    }
  }, []);

  // ---------------------------------------------------------------------------
  // Single-item processing step
  // ---------------------------------------------------------------------------
  const processOneItem = useCallback(
    async (item: EmbossingQueueRecord): Promise<void> => {
      setIsProcessing(true);
      setActiveItem(item);

      const currentItem = item;
      const shouldClaim = item.status === 'WAITING';

      if (shouldClaim) {
        try {
          const updated = await markQueueItemInProgress(item.id);
          setRecords((prev) => prev.map((r) => (r.id === item.id ? updated : r)));
          notifyEmbossingDataUpdated();
        } catch (err) {
          console.warn('markInProgress failed:', err);
          setRecords((prev) => prev.map((r) => (r.id === item.id ? { ...r, status: 'IN_PROGRESS' } : r)));
        }
      }

      try {
        await runPrintRoutine(currentItem);
      } catch (err) {
        if (err instanceof Error && err.message === 'PAUSED') {
          setIsProcessing(false);
          return;
        }
        throw err;
      }

      let completedItem: EmbossingQueueRecord = { ...currentItem, status: 'COMPLETED' };
      try {
        completedItem = await markQueueItemCompleted(currentItem.id);
        notifyEmbossingDataUpdated();
      } catch (err) {
        console.warn('markCompleted fallback to local state:', err);
      }

      setLastCompletedItem(completedItem);
      setActiveItem(null);
      setIsProcessing(false);

      try {
        await loadRecords();
      } catch (err) {
        console.warn('Failed to refresh buffer after completion:', err);
      }
    },
    [loadRecords, runPrintRoutine]
  );

  // ---------------------------------------------------------------------------
  // Execution loop — runs continuously until no WAITING items remain in DB
  // ---------------------------------------------------------------------------
  const runLoop = useCallback(async () => {
    if (loopRunningRef.current) return;
    loopRunningRef.current = true;
    stopRequestedRef.current = false;
    setIsRunning(true);

    while (!stopRequestedRef.current) {
      const currentActive = activeItemRef.current;
      const pausedItem = currentActive ?? recordsRef.current.find((r) => r.status === 'IN_PROGRESS');
      const waitingItem = recordsRef.current.find((r) => r.status === 'WAITING');
      let nextItem = pausedItem ?? waitingItem;

      if (!nextItem) {
        try {
          const claimed = await claimNextWaitingQueueItem();
          if (!claimed) {
            break;
          }
          setRecords((prev) => {
            const merged = [claimed, ...prev.filter((r) => r.id !== claimed.id)];
            return merged.slice(0, BUFFER_SIZE);
          });
          nextItem = claimed;
        } catch (err) {
          console.warn('Failed to claim next waiting item:', err);
          break;
        }
      }

      await processOneItem(nextItem);
      if (stopRequestedRef.current) break;
    }

    loopRunningRef.current = false;
    setIsRunning(false);
    setIsProcessing(false);
  }, [processOneItem]);

  // ---------------------------------------------------------------------------
  // Start (one-shot — runs until all done)
  // ---------------------------------------------------------------------------
  const handleStartEmbossing = useCallback(() => {
    if (isRunning) {
      stopRequestedRef.current = true;
      abortCurrentPrint();
      setIsRunning(false);
      setIsProcessing(false);
      return;
    }

    if (!nextWaitingItem && !pausedItemFromBuffer && !activeItem) return;
    runLoop();
  }, [isRunning, nextWaitingItem, pausedItemFromBuffer, activeItem, runLoop, abortCurrentPrint]);

  // ---------------------------------------------------------------------------
  // Render — UI/CSS identical to original (Reset Queue button removed)
  // ---------------------------------------------------------------------------
  return (
    <div className="animate-fade-in space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-semibold text-white tracking-tight leading-tight flex items-center space-x-3">
            <span>Machine Module</span>
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Industrial Marking Chamber Workpiece &amp; Sequential Embossing Controller
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={loadRecords}
            disabled={loading || isRunning}
            className="p-2.5 rounded-xl border border-[#221e33] bg-[#13111c] hover:bg-[#1a1726] text-gray-300 transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
            title="Refresh buffer from DB"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Main Two-Panel Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

        {/* LEFT PANEL: Machine Module List (5 Columns) */}
        <div className="lg:col-span-5 bg-[#0D0E19] rounded-3xl border border-[#1b172a] shadow-sm p-6 space-y-5">
          <div className="flex items-center justify-between pb-4 border-b border-[#1b172a]">
            <div className="flex items-center space-x-2.5">
              <Layers className="w-5 h-5 text-[#8b5cf6]" />
              <h2 className="text-base font-bold text-white uppercase tracking-wider">
                Machine Module List
              </h2>
            </div>
            <span className="text-xs font-bold text-[#8b5cf6] bg-[#19122a] border border-[#3c1e6d]/30 px-3 py-1 rounded-full">
              {records.length} Records Loaded
            </span>
          </div>

          {/* Record List Items */}
          <div className="space-y-3">
            {records.map((item) => {
              const isWaiting = item.status === 'WAITING';
              const isCompleted = item.status === 'COMPLETED';
              const isInProgress = item.status === 'IN_PROGRESS';
              const isNextToMark = nextWaitingItem?.id === item.id && !isRunning;

              return (
                <div
                  key={item.id}
                  className={`p-4 rounded-2xl border transition-all duration-200 flex items-center justify-between ${
                    isInProgress
                      ? 'bg-indigo-950/30 border-[#5E40FF]/80 shadow-md ring-2 ring-[#5E40FF]/20'
                      : isNextToMark
                      ? 'bg-[#20150b]/40 border-amber-500/30 hover:border-amber-500/50 shadow-sm'
                      : isCompleted
                      ? 'bg-emerald-950/20 border-emerald-900/30 opacity-90'
                      : 'bg-[#13111c] border-[#221e33]'
                  }`}
                >
                  <div className="flex items-center space-x-3.5">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center font-mono text-xs font-black shadow-sm ${
                        isCompleted
                          ? 'bg-emerald-950/40 text-emerald-450 border border-emerald-900/40'
                          : isInProgress
                          ? 'bg-indigo-950/40 text-indigo-400 border border-indigo-900/60 animate-pulse'
                          : isNextToMark
                          ? 'bg-amber-950/40 text-amber-500 border border-amber-900/60 animate-pulse'
                          : 'bg-[#13111c] text-gray-400 border border-[#221e33]'
                      }`}
                    >
                      #{item.id}
                    </div>

                    <div>
                      <div className="text-xs font-bold text-gray-500 uppercase tracking-wider">
                        Workpiece {item.id}
                      </div>
                      <div className="text-sm font-bold text-white font-mono flex items-center space-x-2 mt-0.5">
                        <span className="text-white">{item.serialNumber}</span>
                        <span className="text-gray-500 font-normal">&bull;</span>
                        <span className="text-[#8b5cf6]">{item.partNumber}</span>
                      </div>
                    </div>
                  </div>

                  {/* Status Badge */}
                  <div>
                    {isInProgress ? (
                      <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-indigo-950/40 text-indigo-400 border border-[#5e40ff]/30 uppercase tracking-wider shadow-xs animate-pulse">
                        <Zap className="w-3.5 h-3.5 text-indigo-400" />
                        <span>In Progress</span>
                      </span>
                    ) : isWaiting ? (
                      <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-[#20150b] text-[#f59e0b] border border-[#f59e0b]/20 uppercase tracking-wider shadow-xs">
                        <Clock className="w-3.5 h-3.5 text-[#f59e0b] animate-spin-slow" />
                        <span>Waiting</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-950/40 text-emerald-450 border border-emerald-900/40 uppercase tracking-wider shadow-xs">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-450" />
                        <span>Completed</span>
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Queue summary footer */}
          <div className="pt-3 border-t border-[#1b172a] flex items-center justify-between text-xs text-gray-400 font-medium">
            <span>
              Completed:{' '}
              <strong className="text-emerald-450">
                {records.filter((r) => r.status === 'COMPLETED').length} / {records.length}
              </strong>
            </span>
            <span>
              Waiting:{' '}
              <strong className="text-[#f59e0b]">
                {records.filter((r) => r.status === 'WAITING').length}
              </strong>
            </span>
          </div>
        </div>

        {/* RIGHT PANEL: Industrial Marking Chamber Overlay (7 Columns) */}
        <div className="lg:col-span-7 space-y-5">

          {/* Machine Chamber Main Container */}
          <div className="bg-[#0D0E19] rounded-3xl border border-[#1b172a] shadow-sm p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-[#1b172a]">
              <div className="flex items-center space-x-2.5">
                <Zap className="w-5 h-5 text-[#8b5cf6]" />
                <h2 className="text-base font-bold text-white uppercase tracking-wider">
                  Industrial Marking Chamber
                </h2>
              </div>

              {isProcessing && (
                <span className="flex items-center space-x-1.5 text-xs font-extrabold text-[#f59e0b] bg-[#20150b] px-3 py-1 rounded-full border border-[#f59e0b]/20 animate-pulse">
                  <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping" />
                  <span>EMBOSSING IN PROGRESS</span>
                </span>
              )}
            </div>

            {/* RELATIVE CONTAINER FOR INDUSTRIAL MARKING CHAMBER IMAGE */}
            <div className="relative w-full rounded-2xl overflow-hidden border-4 border-slate-900 bg-slate-950 shadow-2xl min-h-[380px] flex items-center justify-center select-none group">

              {/* Image named "industrial marking chamber" */}
              <img
                src="/assets/industrial_marking_chamber.png"
                alt="industrial marking chamber"
                className="w-full h-auto object-cover opacity-85 min-h-[380px] max-h-[460px] transition-all duration-500 group-hover:scale-[1.01]"
              />

              {/* Grid / HUD Overlay lines on image */}
              <div className="absolute inset-0 bg-[linear-gradient(to_right,#334155_1px,transparent_1px),linear-gradient(to_bottom,#334155_1px,transparent_1px)] bg-[size:32px_32px] opacity-20 pointer-events-none" />

              {/* Laser Reticle Target Box */}
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] h-[75%] border border-cyan-500/30 rounded-xl pointer-events-none flex flex-col justify-between p-2">
                <div className="flex justify-between">
                  <div className="w-3 h-3 border-t-2 border-l-2 border-cyan-400" />
                  <div className="w-3 h-3 border-t-2 border-r-2 border-cyan-400" />
                </div>
                <div className="flex justify-between">
                  <div className="w-3 h-3 border-b-2 border-l-2 border-cyan-400" />
                  <div className="w-3 h-3 border-b-2 border-r-2 border-cyan-400" />
                </div>
              </div>

              {/* ABSOLUTE POSITIONED OVERLAY BOX OVER IMAGE TARGET AREA */}
              <div
                className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-slate-900/90 backdrop-blur-md border-2 ${
                  isProcessing
                    ? 'border-amber-400 shadow-[0_0_35px_rgba(245,158,11,0.6)] scale-105'
                    : activeItem || lastCompletedItem
                    ? 'border-indigo-500/90 shadow-[0_0_30px_rgba(94,64,255,0.5)]'
                    : 'border-slate-700 shadow-xl'
                } rounded-2xl p-6 text-center min-w-[280px] max-w-[90%] transition-all duration-300 z-10`}
              >

                {/* Active Text Header */}
                <div className="text-[11px] font-extrabold uppercase tracking-widest text-slate-400 mb-2 flex items-center justify-center space-x-1.5">
                  <span
                    className={`w-2 h-2 rounded-full ${
                      isProcessing
                        ? 'bg-amber-400 animate-ping'
                        : activeItem
                        ? 'bg-indigo-400'
                        : 'bg-slate-500'
                    }`}
                  />
                  <span>
                    {isProcessing
                      ? 'EMBOSSING ACTIVE ITEM'
                      : displayActiveItem
                      ? `ACTIVE WORKPIECE (ID: #${displayActiveItem.id})`
                      : 'MARKING CHAMBER OVERLAY'}
                  </span>
                </div>

                {/* Display serialNumber & partNumber over image */}
                <div className="space-y-3 my-2 py-2 bg-slate-950/70 rounded-xl border border-slate-800 px-4">
                  <div>
                    <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      SERIAL NUMBER
                    </div>
                    <div className="text-2xl sm:text-3xl font-black font-mono text-indigo-400 tracking-widest drop-shadow-[0_2px_8px_rgba(94,64,255,0.4)]">
                      {displayActiveItem
                        ? displayActiveItem.serialNumber
                        : lastCompletedItem
                        ? lastCompletedItem.serialNumber
                        : nextWaitingItem
                        ? nextWaitingItem.serialNumber
                        : 'SN-XXXX'}
                    </div>
                  </div>

                  <div className="border-t border-slate-800/80 pt-2">
                    <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      PART NUMBER
                    </div>
                    <div className="text-xl sm:text-2xl font-extrabold font-mono text-slate-100 tracking-wider">
                      {displayActiveItem
                        ? displayActiveItem.partNumber
                        : lastCompletedItem
                        ? lastCompletedItem.partNumber
                        : nextWaitingItem
                        ? nextWaitingItem.partNumber
                        : 'PN-XXXX'}
                    </div>
                  </div>
                </div>

                {/* Overlay status tag */}
                <div className="mt-3">
                  {isProcessing ? (
                    <span className="inline-block bg-amber-500/20 text-amber-300 border border-amber-500/50 text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full animate-pulse">
                      STAMPING IN PROGRESS...
                    </span>
                  ) : displayActiveItem && displayActiveItem.status === 'COMPLETED' ? (
                    <span className="inline-block bg-emerald-500/20 text-emerald-300 border border-emerald-500/50 text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full">
                      EMBOSSING COMPLETED
                    </span>
                  ) : nextWaitingItem ? (
                    <span className="inline-block bg-indigo-500/20 text-indigo-300 border border-indigo-500/50 text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full">
                      READY TO EMBOSS
                    </span>
                  ) : (
                    <span className="inline-block bg-emerald-500/20 text-emerald-300 border border-emerald-500/50 text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full">
                      ALL RECORDS COMPLETED
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* EMBOSSING EXECUTION CONTROLS */}
            <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-4">

              {/* Start Embossing Button */}
              <button
                onClick={handleStartEmbossing}
                disabled={isRunning || !nextWaitingItem}
                className={`w-full sm:w-auto flex items-center justify-center space-x-2.5 px-8 py-3.5 rounded-2xl font-extrabold text-base shadow-lg transition-all duration-200 ${
                  isRunning || !nextWaitingItem
                    ? 'bg-[#13111c] text-gray-500 border border-[#221e33] cursor-not-allowed shadow-none'
                    : 'bg-[#7c3aed] hover:bg-[#6d28d9] text-white shadow-purple-500/30 hover:shadow-purple-500/50 hover:scale-[1.02] active:scale-[0.98]'
                }`}
              >
                <Play className={`w-5 h-5 fill-current ${isProcessing ? 'animate-bounce' : ''}`} />
                <span>
                  {isRunning
                    ? 'Pause Embossing'
                    : activeItem || pausedItemFromBuffer
                    ? 'Resume Embossing'
                    : allCompleted
                    ? 'No Data Available'
                    : `Start Embossing (${nextWaitingItem?.serialNumber ?? ''})`}
                </span>
              </button>

              {/* Status helper text */}
              <div className="text-right text-xs font-semibold text-gray-400">
                {noDataAvailable ? (
                  <span className="text-[#f59e0b] font-bold flex items-center justify-end space-x-1">
                    <CheckCheck className="w-4 h-4" />
                    <span>No data available for printing.</span>
                  </span>
                ) : isRunning ? (
                  <span className="text-[#8b5cf6] font-bold">
                    Processing queue continuously...
                  </span>
                ) : activeItem || pausedItemFromBuffer ? (
                  <span className="text-[#f59e0b] font-bold">
                    Paused at workpiece #{(activeItem || pausedItemFromBuffer)?.id}
                  </span>
                ) : nextWaitingItem ? (
                  <span>
                    Next in queue:{' '}
                    <strong className="text-white font-mono">{nextWaitingItem.serialNumber}</strong>{' '}
                    ({nextWaitingItem.partNumber})
                  </span>
                ) : (
                  <span>Ready</span>
                )}
              </div>
            </div>

          </div>

          {/* Instruction Note */}
          <div className="bg-indigo-950/20 border border-[#5e40ff]/10 p-4 rounded-2xl flex items-start space-x-3">
            <AlertCircle className="w-5 h-5 text-[#8b5cf6] shrink-0 mt-0.5" />
            <p className="text-xs text-indigo-300 leading-relaxed font-medium">
              Click <strong>&quot;Start Embossing&quot;</strong> to process all items sequentially from{' '}
              <span className="font-bold text-[#f59e0b] bg-[#20150b] px-1.5 py-0.5 rounded">Waiting</span> to{' '}
              <span className="font-bold text-emerald-450 bg-emerald-950/40 px-1.5 py-0.5 rounded">Completed</span>.
              The loop runs continuously through the rolling 5-item buffer until all database records are finished.
            </p>
          </div>

        </div>

      </div>
    </div>
  );
};

export default MachinePage;
