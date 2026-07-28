import React, { useState, useEffect, useRef } from 'react';
import { 
  Play, 
  RotateCcw, 
  Sliders, 
  Settings, 
  Cpu, 
  Wrench, 
  CheckCircle2, 
  Zap, 
  Volume2, 
  VolumeX
} from 'lucide-react';

interface EmbossingPart {
  id: string;
  partNo: string;
  serialNo: string;
  status: string;
}

export const MachinePage: React.FC = () => {
  // Demo Parts Queue
  const partsQueue: EmbossingPart[] = [
    { id: '1', partNo: 'Pn00111c', serialNo: 'P0011156', status: 'Pending' },
    { id: '2', partNo: 'Pn00112c', serialNo: 'P0011157', status: 'Pending' },
    { id: '3', partNo: 'Pn00113c', serialNo: 'P0011158', status: 'Pending' }
  ];

  // Component State
  const [selectedPartId, setSelectedPartId] = useState<string>('1');
  const [embossingSpeed, setEmbossingSpeed] = useState<number>(50); // 10 to 100 mm/s
  const [markingForce, setMarkingForce] = useState<number>(6.5); // 1.0 to 10.0 N
  const [markingDepth, setMarkingDepth] = useState<number>(0.8); // 0.1 to 1.5 mm
  const [fontType, setFontType] = useState<string>('Dot-matrix'); // 'Dot-matrix' | 'Standard Block'
  const [soundEnabled, setSoundEnabled] = useState<boolean>(false);
  
  // Animation State
  const [simStatus, setSimStatus] = useState<'idle' | 'marking_part' | 'marking_serial' | 'returning' | 'completed'>('idle');
  const [activeCharIndex, setActiveCharIndex] = useState<number>(-1);
  const [completedPartChars, setCompletedPartChars] = useState<boolean[]>([]);
  const [completedSerialChars, setCompletedSerialChars] = useState<boolean[]>([]);
  
  // Visual Gantry Needle Coordinates
  const [needlePos, setNeedlePos] = useState<{ x: number; y: number }>({ x: 30, y: 30 });
  const [sparks, setSparks] = useState<{ id: number; x: number; y: number; size: number }[]>([]);

  // Selected Part values
  const currentPart = partsQueue.find(p => p.id === selectedPartId) || partsQueue[0];
  const { partNo, serialNo } = currentPart;

  // Refs for tracking animation loops
  const timerRef = useRef<any>(null);
  const audioCtxRef = useRef<AudioContext | null>(null);

  // Initialize character state on part change
  useEffect(() => {
    resetSimulation();
  }, [selectedPartId]);

  // Audio synthesizer player for mechanical marking tap sound
  const playMarkingTone = (freq = 400) => {
    if (!soundEnabled) return;
    try {
      if (!audioCtxRef.current) {
        audioCtxRef.current = new (window.AudioContext || (window as any).webkitAudioContext)();
      }
      const ctx = audioCtxRef.current;
      if (ctx.state === 'suspended') {
        ctx.resume();
      }
      
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      
      // Industrial marking sound - short metallic impulse
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(100, ctx.currentTime + 0.08);
      
      gain.gain.setValueAtTime(0.08, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);
      
      osc.connect(gain);
      gain.connect(ctx.destination);
      
      osc.start();
      osc.stop(ctx.currentTime + 0.08);
    } catch (e) {
      console.warn('Audio feedback failed:', e);
    }
  };

  const resetSimulation = () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    setSimStatus('idle');
    setActiveCharIndex(-1);
    setCompletedPartChars(new Array(partNo.length).fill(false));
    setCompletedSerialChars(new Array(serialNo.length).fill(false));
    setNeedlePos({ x: 30, y: 30 }); // Home coordinates
    setSparks([]);
  };

  // Spark generators
  const generateSparks = (x: number, y: number) => {
    const newSparks = Array.from({ length: 6 }).map((_, i) => ({
      id: Date.now() + i,
      x: x + (Math.random() - 0.5) * 12,
      y: y + (Math.random() - 0.5) * 12,
      size: Math.random() * 3 + 1
    }));
    setSparks(prev => [...prev.slice(-10), ...newSparks]);
  };

  // Main simulation effect runner
  const startSimulation = () => {
    resetSimulation();
    
    // Coordinates calculation mapping for SVG visualization
    // SVG metal size is 440 x 180
    
    const partNoY = 75;
    const serialNoY = 125;
    
    const getPartCharX = (idx: number) => {
      const startX = 130;
      const spacing = 22;
      return startX + idx * spacing;
    };

    const getSerialCharX = (idx: number) => {
      const startX = 130;
      const spacing = 22;
      return startX + idx * spacing;
    };

    let stage: 'moving_to_part' | 'marking_part' | 'moving_to_serial' | 'marking_serial' | 'returning' = 'moving_to_part';
    let currentIdx = 0;
    
    // Step speed is inversely proportional to embossingSpeed slider
    const stepDelay = Math.max(100, 1000 - embossingSpeed * 9); 

    const runStep = () => {
      if (stage === 'moving_to_part') {
        setSimStatus('marking_part');
        const targetX = getPartCharX(0);
        const targetY = partNoY;
        
        // Rapid move to first char
        setNeedlePos({ x: targetX, y: targetY });
        stage = 'marking_part';
        currentIdx = 0;
        timerRef.current = setTimeout(runStep, stepDelay / 2);
      } 
      else if (stage === 'marking_part') {
        if (currentIdx < partNo.length) {
          const charX = getPartCharX(currentIdx);
          setNeedlePos({ x: charX, y: partNoY });
          setActiveCharIndex(currentIdx);
          generateSparks(charX, partNoY);
          playMarkingTone(480 + currentIdx * 20);

          setCompletedPartChars(prev => {
            const next = [...prev];
            next[currentIdx] = true;
            return next;
          });

          currentIdx++;
          timerRef.current = setTimeout(runStep, stepDelay);
        } else {
          // Finished part number, move to serial
          stage = 'moving_to_serial';
          setActiveCharIndex(-1);
          timerRef.current = setTimeout(runStep, 400); // pause between rows
        }
      } 
      else if (stage === 'moving_to_serial') {
        setSimStatus('marking_serial');
        const targetX = getSerialCharX(0);
        const targetY = serialNoY;
        setNeedlePos({ x: targetX, y: targetY });
        stage = 'marking_serial';
        currentIdx = 0;
        timerRef.current = setTimeout(runStep, stepDelay / 2);
      } 
      else if (stage === 'marking_serial') {
        if (currentIdx < serialNo.length) {
          const charX = getSerialCharX(currentIdx);
          setNeedlePos({ x: charX, y: serialNoY });
          setActiveCharIndex(currentIdx);
          generateSparks(charX, serialNoY);
          playMarkingTone(380 + currentIdx * 15);

          setCompletedSerialChars(prev => {
            const next = [...prev];
            next[currentIdx] = true;
            return next;
          });

          currentIdx++;
          timerRef.current = setTimeout(runStep, stepDelay);
        } else {
          // Finished serial number, return to home
          stage = 'returning';
          setSimStatus('returning');
          setActiveCharIndex(-1);
          timerRef.current = setTimeout(runStep, 300);
        }
      } 
      else if (stage === 'returning') {
        setNeedlePos({ x: 30, y: 30 });
        setSimStatus('completed');
        // Final chime
        if (soundEnabled) {
          setTimeout(() => playMarkingTone(800), 100);
          setTimeout(() => playMarkingTone(1000), 200);
        }
      }
    };

    // Begin loop
    runStep();
  };

  useEffect(() => {
    // Clean up sparks after rendering
    if (sparks.length > 0) {
      const sparkTimeout = setTimeout(() => {
        setSparks([]);
      }, 500);
      return () => clearTimeout(sparkTimeout);
    }
  }, [sparks]);

  useEffect(() => {
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, []);

  return (
    <div className="animate-fade-in space-y-6">
      
      {/* Top Welcome/Heading Bar */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight flex items-center space-x-2">
            <Cpu className="w-8 h-8 text-[#5E40FF] stroke-[2.5]" />
            <span>Machine Embossing Simulator</span>
          </h1>
          <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
            Configure embossing parameters, adjust fonts, and preview physical markings on tags
          </p>
        </div>

        {/* Live Status Indicators */}
        <div className="flex flex-wrap gap-2.5">
          <div className="flex items-center space-x-2 bg-white border border-gray-150 px-4 py-2 rounded-xl shadow-sm">
            <span className="w-2.5 h-2.5 rounded-full bg-[#00B074] animate-pulse" />
            <span className="text-xs font-bold text-gray-700 uppercase tracking-wide">
              Station 4: Online
            </span>
          </div>

          <button
            onClick={() => setSoundEnabled(!soundEnabled)}
            className={`flex items-center space-x-2 border px-4 py-2 rounded-xl shadow-sm transition-all duration-150 ${
              soundEnabled 
                ? 'bg-amber-50 border-amber-200 text-amber-700 hover:bg-amber-100 font-bold' 
                : 'bg-white border-gray-200 text-gray-500 hover:bg-gray-55 font-semibold'
            }`}
          >
            {soundEnabled ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4" />}
            <span className="text-xs">{soundEnabled ? 'Sound On' : 'Muted'}</span>
          </button>
        </div>
      </div>

      {/* Grid Dashboard */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        
        {/* Left Column: Mechanical / Workpiece Graphic (7 Columns) */}
        <div className="lg:col-span-7 space-y-6">
          
          {/* Main Visualizer Card */}
          <div className="bg-white rounded-3xl border border-gray-200 shadow-md p-6 relative overflow-hidden">
            {/* Visualizer Header */}
            <div className="flex justify-between items-center mb-5 pb-3 border-b border-gray-100">
              <div className="flex items-center space-x-2">
                <Wrench className="w-5 h-5 text-gray-500" />
                <h2 className="text-base font-bold text-gray-800 uppercase tracking-wider">
                  Industrial Marking Chamber
                </h2>
              </div>
              
              {/* XY Readouts */}
              <div className="flex items-center space-x-3 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-200">
                <span className="text-[10px] font-extrabold text-gray-400 tracking-wider">GANTRY AXIS:</span>
                <span className="text-xs font-mono font-bold text-gray-700">
                  X: {needlePos.x.toFixed(1)} mm
                </span>
                <span className="text-xs font-mono font-bold text-gray-700">
                  Y: {needlePos.y.toFixed(1)} mm
                </span>
              </div>
            </div>

            {/* Embossing Gantry Arena */}
            <div className="relative bg-gray-900 rounded-2xl border-4 border-gray-950 p-4 shadow-inner flex items-center justify-center min-h-[300px]">
              
              {/* Laser Grid Background */}
              <div className="absolute inset-0 bg-[linear-gradient(to_right,#1f2937_1px,transparent_1px),linear-gradient(to_bottom,#1f2937_1px,transparent_1px)] bg-[size:24px_24px] opacity-25" />
              
              {/* Dynamic Gantry Guides */}
              <div 
                className="absolute left-0 right-0 h-[2px] bg-red-500/15 pointer-events-none transition-all duration-75"
                style={{ top: `${(needlePos.y / 180) * 100}%` }}
              />
              <div 
                className="absolute top-0 bottom-0 w-[2px] bg-red-500/15 pointer-events-none transition-all duration-75"
                style={{ left: `${(needlePos.x / 440) * 100}%` }}
              />

              {/* Physical Metal Workpiece tag simulation */}
              <div className="relative w-full max-w-[440px] aspect-[440/180] rounded-lg bg-gradient-to-br from-[#E2E8F0] via-[#CBD5E1] to-[#94A3B8] border-2 border-slate-400 shadow-[inset_0_4px_12px_rgba(255,255,255,0.7),0_10px_20px_rgba(0,0,0,0.4)] flex flex-col justify-between p-6 select-none">
                
                {/* Metallic Clamps/Rivets in 4 corners */}
                <div className="absolute top-2 left-2 w-4 h-4 rounded-full bg-slate-500 border border-slate-600 shadow-[inset_0_1px_3px_rgba(255,255,255,0.5)] flex items-center justify-center">
                  <div className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                </div>
                <div className="absolute top-2 right-2 w-4 h-4 rounded-full bg-slate-500 border border-slate-600 shadow-[inset_0_1px_3px_rgba(255,255,255,0.5)] flex items-center justify-center">
                  <div className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                </div>
                <div className="absolute bottom-2 left-2 w-4 h-4 rounded-full bg-slate-500 border border-slate-600 shadow-[inset_0_1px_3px_rgba(255,255,255,0.5)] flex items-center justify-center">
                  <div className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                </div>
                <div className="absolute bottom-2 right-2 w-4 h-4 rounded-full bg-slate-500 border border-slate-600 shadow-[inset_0_1px_3px_rgba(255,255,255,0.5)] flex items-center justify-center">
                  <div className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                </div>

                {/* Manufacturer Branding Logo on Tag */}
                <div className="flex justify-between items-center opacity-60">
                  <div className="text-[10px] font-black tracking-widest text-slate-800">
                    ARC INDUSTRIAL CORP
                  </div>
                  <div className="text-[8px] font-bold text-slate-700">
                    MADE IN USA
                  </div>
                </div>

                {/* Part Number Label & Stamped Text */}
                <div className="my-2">
                  <div className="text-[10px] font-black text-slate-600 tracking-wider mb-1 uppercase">
                    PART NUMBER:
                  </div>
                  <div className="flex space-x-1.5 font-mono text-[22px] font-black tracking-widest h-8 items-center pl-4">
                    {partNo.split('').map((char, index) => {
                      const isCompleted = completedPartChars[index];
                      const isActive = simStatus === 'marking_part' && activeCharIndex === index;
                      
                      return (
                        <span 
                          key={index} 
                          className={`relative inline-block transition-all duration-150 ${
                            isCompleted 
                              ? 'text-slate-950 scale-100 filter drop-shadow-[1px_2px_1px_rgba(255,255,255,0.8)] [text-shadow:inset_0_2px_2px_rgba(0,0,0,0.6)] font-extrabold' 
                              : 'text-slate-400/40 border-b border-dashed border-slate-500/20'
                          } ${isActive ? 'text-[#5E40FF] scale-125 font-bold' : ''}`}
                          style={{
                            fontFamily: fontType === 'Dot-matrix' ? '"Courier New", Courier, monospace' : 'inherit',
                          }}
                        >
                          {char}
                        </span>
                      );
                    })}
                  </div>
                </div>

                {/* Serial Number Label & Stamped Text */}
                <div className="my-2">
                  <div className="text-[10px] font-black text-slate-600 tracking-wider mb-1 uppercase">
                    SERIAL NUMBER:
                  </div>
                  <div className="flex space-x-1.5 font-mono text-[22px] font-black tracking-widest h-8 items-center pl-4">
                    {serialNo.split('').map((char, index) => {
                      const isCompleted = completedSerialChars[index];
                      const isActive = simStatus === 'marking_serial' && activeCharIndex === index;
                      
                      return (
                        <span 
                          key={index} 
                          className={`relative inline-block transition-all duration-150 ${
                            isCompleted 
                              ? 'text-slate-955 scale-100 filter drop-shadow-[1px_2px_1px_rgba(255,255,255,0.8)] [text-shadow:inset_0_2px_2px_rgba(0,0,0,0.6)] font-extrabold' 
                              : 'text-slate-400/40 border-b border-dashed border-slate-500/20'
                          } ${isActive ? 'text-[#5E40FF] scale-125 font-bold' : ''}`}
                          style={{
                            fontFamily: fontType === 'Dot-matrix' ? '"Courier New", Courier, monospace' : 'inherit',
                          }}
                        >
                          {char}
                        </span>
                      );
                    })}
                  </div>
                </div>

                {/* Tag Footer details */}
                <div className="flex justify-between items-center text-[9px] font-bold text-slate-700 opacity-60">
                  <span>SPECIFICATION ID: Q-4981C</span>
                  <span>DEPTH LIMIT: {markingDepth.toFixed(2)} MM</span>
                </div>
              </div>

              {/* Physical Embossing Pin Overlay */}
              <div 
                className="absolute pointer-events-none transition-all duration-75 z-20"
                style={{ 
                  left: `calc(${(needlePos.x / 440) * 100}% - 14px)`, 
                  top: `calc(${(needlePos.y / 180) * 100}% - 14px)` 
                }}
              >
                {/* Marking Stylus Visual */}
                <svg width="28" height="28" viewBox="0 0 28 28">
                  {/* Outer casing */}
                  <circle cx="14" cy="14" r="10" fill="#374151" stroke="#6b7280" strokeWidth="2" opacity="0.85" />
                  {/* Metal Pin core */}
                  <circle cx="14" cy="14" r="4" fill="#9ca3af" stroke="#f3f4f6" strokeWidth="1" />
                  {/* Red/Yellow laser alignment dot */}
                  <circle 
                    cx="14" 
                    cy="14" 
                    r="1.5" 
                    fill={simStatus.startsWith('marking') ? '#FF3E3E' : '#EAB308'} 
                    className={simStatus.startsWith('marking') ? 'animate-pulse' : ''} 
                  />
                  {/* Stylus Pointer Crosshair */}
                  <line x1="14" y1="0" x2="14" y2="6" stroke="#ef4444" strokeWidth="1.5" />
                  <line x1="14" y1="22" x2="14" y2="28" stroke="#ef4444" strokeWidth="1.5" />
                  <line x1="0" y1="14" x2="6" y2="14" stroke="#ef4444" strokeWidth="1.5" />
                  <line x1="22" y1="14" x2="28" y2="14" stroke="#ef4444" strokeWidth="1.5" />
                </svg>

                {/* Status tag */}
                {simStatus.startsWith('marking') && (
                  <div className="absolute left-6 top-1/2 -translate-y-1/2 bg-red-600 text-white font-bold text-[8px] tracking-wider px-1.5 py-0.5 rounded shadow whitespace-nowrap animate-pulse">
                    STAMPING PIN
                  </div>
                )}
              </div>

              {/* Sparks layer */}
              {sparks.map(spark => (
                <div
                  key={spark.id}
                  className="absolute rounded-full bg-amber-400 pointer-events-none z-30"
                  style={{
                    left: `calc(${(spark.x / 440) * 100}% - 2px)`,
                    top: `calc(${(spark.y / 180) * 100}% - 2px)`,
                    width: `${spark.size}px`,
                    height: `${spark.size}px`,
                    boxShadow: '0 0 6px #F59E0B, 0 0 12px #F59E0B'
                  }}
                />
              ))}
            </div>

            {/* Sim Control Actions */}
            <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
              <div className="flex space-x-2">
                <button
                  onClick={startSimulation}
                  disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                  className={`flex items-center space-x-2 px-6 py-2.5 rounded-xl font-bold text-sm shadow-sm transition-all duration-150 ${
                    simStatus.startsWith('marking') || simStatus === 'returning'
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                      : 'bg-[#5E40FF] hover:bg-[#4d32e6] text-white hover:shadow-md'
                  }`}
                >
                  <Play className="w-4 h-4 fill-current" />
                  <span>
                    {simStatus === 'completed' ? 'Re-run Embossing' : 'Start Embossing'}
                  </span>
                </button>

                <button
                  onClick={resetSimulation}
                  className="flex items-center space-x-2 bg-white hover:bg-gray-50 border border-gray-200 text-gray-700 px-4 py-2.5 rounded-xl font-bold text-sm shadow-sm transition-all duration-150"
                >
                  <RotateCcw className="w-4 h-4" />
                  <span>Reset Tag</span>
                </button>
              </div>

              {/* Progress display */}
              <div className="text-right">
                <div className="text-xs font-semibold text-gray-500 mb-1">
                  Simulation Status
                </div>
                <div className="text-sm font-bold text-gray-800 flex items-center justify-end space-x-1.5">
                  {simStatus === 'idle' && (
                    <span className="text-gray-500">Ready to mark</span>
                  )}
                  {simStatus === 'marking_part' && (
                    <span className="text-[#5E40FF] flex items-center">
                      <Zap className="w-3.5 h-3.5 mr-1 text-amber-500 fill-amber-500 animate-bounce" />
                      Embossing Part Number
                    </span>
                  )}
                  {simStatus === 'marking_serial' && (
                    <span className="text-[#5E40FF] flex items-center">
                      <Zap className="w-3.5 h-3.5 mr-1 text-amber-500 fill-amber-500 animate-bounce" />
                      Embossing Serial Number
                    </span>
                  )}
                  {simStatus === 'returning' && (
                    <span className="text-yellow-600">Returning pin...</span>
                  )}
                  {simStatus === 'completed' && (
                    <span className="text-[#00B074] flex items-center">
                      <CheckCircle2 className="w-4 h-4 mr-1 text-[#00B074]" />
                      Embossing Complete
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

        </div>

        {/* Right Column: Parameters (5 Columns) */}
        <div className="lg:col-span-5 space-y-6">
          
          {/* Part Selection Dropdown */}
          <div className="bg-white rounded-3xl border border-gray-200 shadow-md p-6">
            <h2 className="text-base font-bold text-gray-800 uppercase tracking-wider mb-4 flex items-center space-x-2">
              <Sliders className="w-5 h-5 text-[#5E40FF]" />
              <span>Select Part from Queue</span>
            </h2>

            <p className="text-xs text-gray-500 mb-4 leading-relaxed">
              Choose an active batch workpiece to simulate marking output. These values represent actual data awaiting embossing in this shift.
            </p>

            <div className="space-y-2">
              {partsQueue.map(part => {
                const isSelected = part.id === selectedPartId;
                return (
                  <button
                    key={part.id}
                    onClick={() => {
                      if (!simStatus.startsWith('marking') && simStatus !== 'returning') {
                        setSelectedPartId(part.id);
                      }
                    }}
                    disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                    className={`w-full text-left p-3.5 rounded-2xl border transition-all duration-150 flex items-center justify-between ${
                      isSelected
                        ? 'bg-indigo-50/50 border-[#5E40FF] shadow-sm'
                        : 'bg-white border-gray-150 hover:bg-gray-50 text-gray-700'
                    } ${
                      (simStatus.startsWith('marking') || simStatus === 'returning') && !isSelected
                        ? 'opacity-50 cursor-not-allowed'
                        : 'cursor-pointer'
                    }`}
                  >
                    <div>
                      <div className="text-xs font-bold text-gray-400 uppercase">Part {part.id}</div>
                      <div className="text-sm font-bold text-gray-800 font-mono mt-0.5">
                        {part.partNo} &bull; {part.serialNo}
                      </div>
                    </div>

                    <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-extrabold bg-[#FEF3C7] text-[#D97706] uppercase tracking-wider">
                      {part.status}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Marking Parameters Settings */}
          <div className="bg-white rounded-3xl border border-gray-200 shadow-md p-6">
            <h2 className="text-base font-bold text-gray-800 uppercase tracking-wider mb-5 flex items-center space-x-2">
              <Settings className="w-5 h-5 text-[#5E40FF]" />
              <span>Marking Configurations</span>
            </h2>

            <div className="space-y-4">
              
              {/* Slider 1: Embossing speed */}
              <div>
                <div className="flex justify-between text-xs font-bold text-gray-700 mb-1.5">
                  <span>Marking Speed</span>
                  <span className="font-mono text-gray-500 font-semibold">{embossingSpeed} mm/s</span>
                </div>
                <input
                  type="range"
                  min="10"
                  max="100"
                  value={embossingSpeed}
                  onChange={(e) => setEmbossingSpeed(parseInt(e.target.value))}
                  disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                  className="w-full accent-[#5E40FF] cursor-pointer"
                />
                <div className="flex justify-between text-[10px] text-gray-400 font-medium mt-1">
                  <span>Precision (10 mm/s)</span>
                  <span>Draft (100 mm/s)</span>
                </div>
              </div>

              {/* Slider 2: Solenoid Force */}
              <div>
                <div className="flex justify-between text-xs font-bold text-gray-700 mb-1.5">
                  <span>Pin Impact Force</span>
                  <span className="font-mono text-gray-500 font-semibold">{markingForce.toFixed(1)} kN</span>
                </div>
                <input
                  type="range"
                  min="10"
                  max="100"
                  value={markingForce * 10}
                  onChange={(e) => setMarkingForce(parseInt(e.target.value) / 10)}
                  disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                  className="w-full accent-[#5E40FF] cursor-pointer"
                />
                <div className="flex justify-between text-[10px] text-gray-400 font-medium mt-1">
                  <span>Soft (1.0 kN)</span>
                  <span>Hard Steel (10.0 kN)</span>
                </div>
              </div>

              {/* Slider 3: Depth */}
              <div>
                <div className="flex justify-between text-xs font-bold text-gray-700 mb-1.5">
                  <span>Target Penetration Depth</span>
                  <span className="font-mono text-gray-500 font-semibold">{markingDepth.toFixed(2)} mm</span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="15"
                  value={markingDepth * 10}
                  onChange={(e) => setMarkingDepth(parseInt(e.target.value) / 10)}
                  disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                  className="w-full accent-[#5E40FF] cursor-pointer"
                />
                <div className="flex justify-between text-[10px] text-gray-400 font-medium mt-1">
                  <span>Light (0.1 mm)</span>
                  <span>Deep (1.5 mm)</span>
                </div>
              </div>

              {/* Font Family selector */}
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-2">
                  Engraving Font Style
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {['Dot-matrix', 'Standard Block'].map((font) => (
                    <button
                      key={font}
                      onClick={() => setFontType(font)}
                      disabled={simStatus.startsWith('marking') || simStatus === 'returning'}
                      className={`py-2 px-3 text-xs font-bold rounded-xl border transition-all duration-150 ${
                        fontType === font
                          ? 'bg-[#EBFDF5] border-[#00B074] text-[#00B074] shadow-sm'
                          : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                      }`}
                    >
                      {font}
                    </button>
                  ))}
                </div>
              </div>

            </div>
          </div>

        </div>

      </div>

    </div>
  );
};

export default MachinePage;
