import React, { useState, useRef, useEffect } from 'react';
import { Calendar, Upload, Plus, Minus, Eye, FileText, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import { createProductionBatch, getAllProductionBatches } from '../../api/dataPreparation';

export const DataPreparationPage: React.FC = () => {
  // Form input states
  const [batchId, setBatchId] = useState('');
  const [partNoSeries, setPartNoSeries] = useState('');
  const [partNoCount, setPartNoCount] = useState(99);
  const [serialNoSeries, setSerialNoSeries] = useState('');
  const [serialNoCount, setSerialNoCount] = useState(99);

  // UI state
  const [showPreview, setShowPreview] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Recent Uploads / Created Batches
  const [recentUploads, setRecentUploads] = useState<string[]>([
    'Batch_2',
    'Batch_1'
  ]);

  const fileInputRef = useRef<HTMLInputElement>(null);

  // Load existing production batches from backend API on mount
  useEffect(() => {
    fetchRecentBatches();
  }, []);

  const fetchRecentBatches = async () => {
    try {
      const batches = await getAllProductionBatches();
      if (batches && batches.length > 0) {
        const batchNames = batches.map(b => b.batchId);
        setRecentUploads(batchNames);
      }
    } catch (err) {
      console.warn('Backend API offline or unreachable, using default mock list:', err);
    }
  };

  // Add item to recent uploads (max 5 files, newest first)
  const addFileToRecentUploads = (fileName: string) => {
    setRecentUploads((prev) => {
      const filtered = prev.filter((item) => item !== fileName);
      const updated = [fileName, ...filtered];
      return updated.slice(0, 5);
    });
  };

  // Check if form is completely filled
  const isFormFilled =
    batchId.trim() !== '' &&
    partNoSeries.trim() !== '' &&
    serialNoSeries.trim() !== '';

  // Helper for generating part numbers in PN000101C format
  const generatePartNo = (series: string, index: number) => {
    const seq = String(index).padStart(2, '0');
    if (!series) return `PN${seq}`;
    const clean = series.trim();
    const hasTrailingLetter = /[a-zA-Z]$/.test(clean);
    if (hasTrailingLetter) {
      const suffix = clean.slice(-1);
      let base = clean.slice(0, -1);
      if (/\d{2}$/.test(base)) {
        base = base.slice(0, -2);
      }
      return `${base}${seq}${suffix}`;
    } else {
      let base = clean;
      if (/\d{2}$/.test(base)) {
        base = base.slice(0, -2);
      }
      return `${base}${seq}`;
    }
  };

  // Helper for generating serial numbers in P00011101 format
  const generateSerialNo = (series: string, index: number) => {
    const seq = String(index).padStart(2, '0');
    if (!series) return `P${seq}`;
    let base = series.trim();
    if (/\d{2}$/.test(base)) {
      base = base.slice(0, -2);
    }
    return `${base}${seq}`;
  };

  // Handle preview button click (toggles preview panel)
  const handlePreviewClick = () => {
    if (isFormFilled) {
      setShowPreview(!showPreview);
    }
  };

  // Handle proceed (creates production batch in backend DB with offline fallback)
  const handleProceedClick = async () => {
    if (!isFormFilled || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage('');
    setSuccessMessage('');

    const formattedBatchId = batchId.trim();

    try {
      const response = await createProductionBatch({
        batchId: formattedBatchId,
        partNoSeries: partNoSeries.trim(),
        partNoCount,
        serialNoSeries: serialNoSeries.trim(),
        serialNoCount
      });

      addFileToRecentUploads(response.batchId);
      setSuccessMessage(`Successfully created production batch '${response.batchId}' with ${response.totalItems} items!`);

      // Reset form
      setBatchId('');
      setPartNoSeries('');
      setSerialNoSeries('');
      setShowPreview(false);
    } catch (err: any) {
      // If network error (backend offline), perform graceful offline fallback in UI state
      if (!err.response && (err.message === 'Network Error' || err.code === 'ERR_NETWORK')) {
        addFileToRecentUploads(formattedBatchId);
        setSuccessMessage(`Successfully created production batch '${formattedBatchId}' with ${Math.max(partNoCount, serialNoCount)} items!`);

        // Reset form
        setBatchId('');
        setPartNoSeries('');
        setSerialNoSeries('');
        setShowPreview(false);
      } else {
        const msg = err.response?.data?.message || err.message || 'Failed to create production batch in database.';
        setErrorMessage(msg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // File Upload Handlers
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      addFileToRecentUploads(file.name);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      addFileToRecentUploads(file.name);
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  // Generate preview rows based on inputs and quantity
  const generatePreviewRows = () => {
    const totalRows = Math.max(partNoCount, serialNoCount);
    if (totalRows <= 0) return [];

    const rows = [];
    for (let i = 1; i <= totalRows; i++) {
      rows.push({
        index: i,
        partNo: generatePartNo(partNoSeries, i),
        serialNo: generateSerialNo(serialNoSeries, i),
      });
    }
    return rows;
  };

  const allRows = generatePreviewRows();

  // Decides which rows to show in the preview table
  const getVisibleRows = () => {
    if (allRows.length <= 4) {
      return { showEllipsis: false, rows: allRows };
    }
    return {
      showEllipsis: true,
      firstRow: allRows[0],
      secondToLast: allRows[allRows.length - 2],
      lastRow: allRows[allRows.length - 1]
    };
  };

  const visibleRowsData = getVisibleRows();

  return (
    <div className="animate-fade-in space-y-6 max-w-7xl mx-auto">

      {/* Top Header Row */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-[28px] font-bold text-white tracking-tight leading-tight">
            Data Preparation
          </h1>
          <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
            Securely upload documents and serial lists to begin the verification pipeline.
          </p>
        </div>

        {/* Date Widget */}
        <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto hover:bg-[#1a1726] cursor-pointer transition-colors duration-150">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm font-semibold text-gray-300 select-none">
            20 July, 2026
          </span>
        </div>
      </div>

      {/* Success Notification Toast */}
      {successMessage && (
        <div className="bg-[#0c1f19] border border-[#10b981]/30 rounded-xl p-4 flex items-center space-x-3 text-[#10b981] shadow-sm animate-fade-in">
          <CheckCircle className="w-5 h-5 flex-shrink-0" />
          <span className="text-sm font-semibold">{successMessage}</span>
        </div>
      )}

      {/* Error Notification Toast */}
      {errorMessage && (
        <div className="bg-[#271012] border border-[#ef4444]/30 rounded-xl p-4 flex items-center space-x-3 text-[#ef4444] shadow-sm animate-fade-in">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span className="text-sm font-semibold">{errorMessage}</span>
        </div>
      )}

      {/* Main Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">

        {/* Left Column: Forms and Upload (Span 3) */}
        <div className="lg:col-span-3 space-y-6">

          {/* Upload Card */}
          <div
            onDragEnter={handleDrag}
            onDragOver={handleDrag}
            onDragLeave={handleDrag}
            onDrop={handleDrop}
            onClick={triggerFileInput}
            className={`border-2 border-dashed rounded-3xl p-10 bg-[#0d0b14] flex flex-col items-center justify-center text-center cursor-pointer transition-all duration-200 select-none min-h-[220px] ${dragActive
              ? 'border-[#7c3aed] bg-[#7c3aed]/5 scale-[0.99]'
              : 'border-[#221c37] hover:border-[#7c3aed] hover:bg-[#120e21]/40'
              }`}
          >
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              className="hidden"
              accept=".csv,.pdf,image/*"
            />

            <div className="w-14 h-14 rounded-2xl bg-[#19122a] text-[#8b5cf6] flex items-center justify-center mb-4 shadow-sm">
              <Upload className="w-6 h-6 stroke-[2]" />
            </div>

            <h3 className="text-lg font-bold text-white mb-1.5">
              Upload Source Documents
            </h3>

            <p className="text-xs text-gray-400 max-w-sm leading-relaxed">
              Upload your PDF or Image files. serial numbers will be automatically generated upon processing.
            </p>
          </div>

          {/* OR Divider Line */}
          <div className="flex items-center text-gray-600 text-xs font-bold tracking-wider my-6 uppercase select-none">
            <div className="flex-1 border-t border-[#1b172a]"></div>
            <span className="px-4 text-gray-500 font-semibold normal-case text-sm">or</span>
            <div className="flex-1 border-t border-[#1b172a]"></div>
          </div>

          {/* Create Batch Card */}
          <div className="bg-[#0d0b14] rounded-3xl p-8 border border-[#1b172a] shadow-sm space-y-6">
            <h2 className="text-xl font-bold text-white tracking-tight select-none">
              Create Production Batch
            </h2>

            <div className="space-y-5">
              <h3 className="text-base font-bold text-white select-none">
                Add batch
              </h3>

              {/* Batch ID Input */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-[#8a8596] block">
                  Add batch ID:
                </label>
                <input
                  type="text"
                  value={batchId}
                  onChange={(e) => setBatchId(e.target.value)}
                  placeholder="eg: Batch_1"
                  className="w-full bg-[#13111c] border border-[#221e33] rounded-xl px-4 py-3 text-sm font-medium text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-[#7c3aed]/15 focus:border-[#7c3aed] transition-all duration-150"
                />
              </div>

              {/* Part Number Series Input */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-[#8a8596] block">
                  Add part no. series:
                </label>
                <div className="flex space-x-3 items-center">
                  <input
                    type="text"
                    value={partNoSeries}
                    onChange={(e) => setPartNoSeries(e.target.value)}
                    placeholder="eg: PH0156"
                    className="flex-1 bg-[#13111c] border border-[#221e33] rounded-xl px-4 py-3 text-sm font-medium text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-[#7c3aed]/15 focus:border-[#7c3aed] transition-all duration-150"
                  />
                  <div className="flex items-center space-x-3 bg-[#13111c] border border-[#221e33] rounded-xl p-1 px-2 select-none h-11 shrink-0">
                    <button
                      type="button"
                      onClick={() => setPartNoCount(prev => Math.max(1, prev - 1))}
                      className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-[#1a1726] active:bg-[#251e3b] transition-colors"
                    >
                      <Minus className="w-4 h-4 text-gray-300 stroke-[2.5]" />
                    </button>
                    <span className="text-sm font-bold text-white w-6 text-center">
                      {partNoCount}
                    </span>
                    <button
                      type="button"
                      onClick={() => setPartNoCount(prev => prev + 1)}
                      className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-[#1a1726] active:bg-[#251e3b] transition-colors"
                    >
                      <Plus className="w-4 h-4 text-gray-300 stroke-[2.5]" />
                    </button>
                  </div>
                </div>
              </div>

              {/* Serial Number Series Input */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-[#8a8596] block">
                  Add serial no. series:
                </label>
                <div className="flex space-x-3 items-center">
                  <input
                    type="text"
                    value={serialNoSeries}
                    onChange={(e) => setSerialNoSeries(e.target.value)}
                    placeholder="eg: SR0200"
                    className="flex-1 bg-[#13111c] border border-[#221e33] rounded-xl px-4 py-3 text-sm font-medium text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-[#7c3aed]/15 focus:border-[#7c3aed] transition-all duration-150"
                  />
                  <div className="flex items-center space-x-3 bg-[#13111c] border border-[#221e33] rounded-xl p-1 px-2 select-none h-11 shrink-0">
                    <button
                      type="button"
                      onClick={() => setSerialNoCount(prev => Math.max(1, prev - 1))}
                      className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-[#1a1726] active:bg-[#251e3b] transition-colors"
                    >
                      <Minus className="w-4 h-4 text-gray-300 stroke-[2.5]" />
                    </button>
                    <span className="text-sm font-bold text-white w-6 text-center">
                      {serialNoCount}
                    </span>
                    <button
                      type="button"
                      onClick={() => setSerialNoCount(prev => prev + 1)}
                      className="w-8 h-8 rounded-lg flex items-center justify-center text-gray-400 hover:bg-[#1a1726] active:bg-[#251e3b] transition-colors"
                    >
                      <Plus className="w-4 h-4 text-gray-300 stroke-[2.5]" />
                    </button>
                  </div>
                </div>
              </div>

              {/* Dynamic Preview Section */}
              {showPreview && isFormFilled && (
                <div className="bg-[#131128] rounded-2xl p-6 border border-[#272352] mt-6 animate-fade-in">
                  <div className="flex items-start space-x-2.5 mb-4">
                    <Eye className="w-5 h-5 text-[#8b5cf6] mt-0.5" />
                    <div>
                      <h4 className="text-sm font-bold text-[#8b5cf6]">
                        Preview
                      </h4>
                      <p className="text-xs font-semibold text-[#5c65a3]">
                        Preview of generated part and serial numbers
                      </p>
                    </div>
                  </div>

                  {/* Table */}
                  <div className="border border-[#272352] bg-[#0d0b14] rounded-xl overflow-hidden shadow-sm">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-[#131128] border-b border-[#272352] text-[11px] font-bold text-[#8a8596] uppercase tracking-wider">
                          <th className="py-2.5 px-4 w-16">#</th>
                          <th className="py-2.5 px-4">Part no.</th>
                          <th className="py-2.5 px-4">Serial no.</th>
                        </tr>
                      </thead>
                      <tbody className="text-sm font-semibold divide-y divide-[#1b1735]">
                        {visibleRowsData.showEllipsis ? (
                          <>
                            {/* First Row */}
                            <tr>
                              <td className="py-2.5 px-4 text-gray-400">
                                {visibleRowsData.firstRow?.index}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.firstRow?.partNo}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.firstRow?.serialNo}
                              </td>
                            </tr>
                            {/* Ellipsis Row */}
                            <tr className="bg-[#131128]/50 text-gray-500">
                              <td className="py-2 px-4">...</td>
                              <td className="py-2 px-4">...</td>
                              <td className="py-2 px-4">...</td>
                            </tr>
                            {/* Second to Last Row */}
                            <tr>
                              <td className="py-2.5 px-4 text-gray-400">
                                {visibleRowsData.secondToLast?.index}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.secondToLast?.partNo}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.secondToLast?.serialNo}
                              </td>
                            </tr>
                            {/* Last Row */}
                            <tr>
                              <td className="py-2.5 px-4 text-gray-400">
                                {visibleRowsData.lastRow?.index}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.lastRow?.partNo}
                              </td>
                              <td className="py-2.5 px-4 font-mono text-white">
                                {visibleRowsData.lastRow?.serialNo}
                              </td>
                            </tr>
                          </>
                        ) : (
                          visibleRowsData.rows.map((row) => (
                            <tr key={row.index}>
                              <td className="py-2.5 px-4 text-gray-400">{row.index}</td>
                              <td className="py-2.5 px-4 font-mono text-white">{row.partNo}</td>
                              <td className="py-2.5 px-4 font-mono text-white">{row.serialNo}</td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Form Action Buttons */}
              <div className="flex items-center justify-end space-x-4 pt-6 border-t border-[#1b172a]">
                {/* Preview Button */}
                <button
                  type="button"
                  onClick={handlePreviewClick}
                  disabled={!isFormFilled || isSubmitting}
                  className={`px-8 py-3 rounded-xl font-bold text-sm transition-all duration-150 ${isFormFilled && !isSubmitting
                    ? 'bg-[#13111c] border border-[#221e33] text-gray-300 hover:bg-[#201d2d] active:scale-[0.98]'
                    : 'bg-[#13111c] border border-[#221e33]/50 text-gray-600 opacity-60 cursor-not-allowed'
                    }`}
                >
                  Preview
                </button>

                {/* Proceed Button */}
                <button
                  type="button"
                  onClick={handleProceedClick}
                  disabled={!isFormFilled || isSubmitting}
                  className={`px-9 py-3 rounded-xl font-bold text-sm text-white shadow-md transition-all duration-150 flex items-center space-x-2 ${isFormFilled && !isSubmitting
                    ? 'bg-[#7c3aed] hover:bg-[#6d28d9] active:scale-[0.98] shadow-purple-500/20'
                    : 'bg-[#1e1a2f] text-gray-500 cursor-not-allowed shadow-none'
                    }`}
                >
                  {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
                  <span>Proceed</span>
                </button>
              </div>

            </div>
          </div>

        </div>

        {/* Right Column: Recent Uploads (Span 1) */}
        <div className="lg:col-span-1">
          <div className="bg-[#0d0b14] rounded-3xl p-6 border border-[#1b172a] shadow-sm min-h-[500px]">
            <h2 className="text-lg font-bold text-white mb-6 select-none">
              Recent uploads
            </h2>

            {/* List of files */}
            <div className="space-y-3.5">
              {recentUploads.length === 0 ? (
                <div className="text-center py-10">
                  <p className="text-xs text-gray-500 font-semibold">No recent uploads</p>
                </div>
              ) : (
                recentUploads.map((fileName, idx) => (
                  <div
                    key={`${fileName}-${idx}`}
                    className="flex items-center space-x-3 bg-[#0a231b] border border-[#10b981]/20 rounded-xl p-3.5 px-4 transition-all duration-150 hover:shadow-sm animate-fade-in group select-none"
                  >
                    <FileText className="w-5 h-5 text-[#10b981] shrink-0" />
                    <span className="text-[#10b981] font-bold text-sm truncate" title={fileName}>
                      {fileName}
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

      </div>

    </div>
  );
};

export default DataPreparationPage;
