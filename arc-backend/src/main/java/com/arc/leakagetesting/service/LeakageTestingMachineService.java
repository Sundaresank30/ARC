package com.arc.leakagetesting.service;

import com.arc.datapreparation.entity.ProductionBatch;
import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.entity.SourceDocument;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.datapreparation.repository.SourceDocumentRepository;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.dto.LeakageMachineDto;
import com.arc.leakagetesting.entity.LeakageTestResult;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeakageTestingMachineService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd MMM");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    private final EmbossingJobRepository embossingJobRepository;
    private final ProductionBatchItemRepository productionItemRepository;
    private final ProductionBatchRepository productionBatchRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final LeakageTestResultRepository resultRepository;
    private final com.arc.leakagetesting.repository.PassedLeakageTestResultRepository passedResultRepository;
    private final com.arc.leakagetesting.repository.FailedLeakageTestResultRepository failedResultRepository;
    private final com.arc.leakagetesting.repository.LeakageMachineReadingRepository readingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.arc.dashboard.service.DashboardService dashboardService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> runningTask = null;

    private volatile String machineStatus = "IDLE"; // IDLE, TESTING, PAUSED, COMPLETED
    private volatile String activeBatchId = null;
    private volatile Long currentProcessingJobId = null;
    private volatile Double livePressure = null;

    public LeakageTestingMachineService(
            EmbossingJobRepository embossingJobRepository,
            ProductionBatchItemRepository productionItemRepository,
            ProductionBatchRepository productionBatchRepository,
            SourceDocumentRepository sourceDocumentRepository,
            LeakageTestResultRepository resultRepository,
            com.arc.leakagetesting.repository.PassedLeakageTestResultRepository passedResultRepository,
            com.arc.leakagetesting.repository.FailedLeakageTestResultRepository failedResultRepository,
            com.arc.leakagetesting.repository.LeakageMachineReadingRepository readingRepository,
            @Autowired(required = false) SimpMessagingTemplate messagingTemplate,
            @Autowired(required = false) com.arc.dashboard.service.DashboardService dashboardService) {
        this.embossingJobRepository = embossingJobRepository;
        this.productionItemRepository = productionItemRepository;
        this.productionBatchRepository = productionBatchRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.resultRepository = resultRepository;
        this.passedResultRepository = passedResultRepository;
        this.failedResultRepository = failedResultRepository;
        this.readingRepository = readingRepository;
        this.messagingTemplate = messagingTemplate;
        this.dashboardService = dashboardService;
    }

    // -------------------------------------------------------------------------
    // Machine State & Dashboard Aggregation
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public LeakageMachineDto.MachineState getMachineState() {
        String batchId = resolveActiveBatchId();
        if (batchId == null || "No Active Batch".equalsIgnoreCase(batchId)) {
            return LeakageMachineDto.MachineState.builder()
                    .machineStatus(machineStatus)
                    .activeBatch("No Active Batch")
                    .fileName("No File")
                    .warningThreshold(null)
                    .alarmThreshold(null)
                    .unit(null)
                    .totalEmbossed(0L)
                    .totalTested(0L)
                    .passedParts(0L)
                    .failedParts(0L)
                    .progressPercent(0.0)
                    .activeChamber(null)
                    .trendData(List.of())
                    .queue(List.of())
                    .history(List.of())
                    .build();
        }

        ThresholdConfig thresholds = getThresholdsForBatch(batchId);

        List<EmbossingJob> readyJobs = getReadyJobsForBatch(batchId);
        List<LeakageTestResult> testedResults = resultRepository.findByBatchIdOrderByIdAsc(batchId);

        long totalEmbossed = readyJobs.size();
        long totalTested = testedResults.size();
        long passedParts = testedResults.stream().filter(r -> "PASSED".equalsIgnoreCase(r.getStatus())).count();
        long failedParts = testedResults.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        double progressPercent = totalEmbossed > 0 ? Math.round((double) totalTested / totalEmbossed * 100.0) : 0.0;

        LeakageMachineDto.LiveChamber activeChamber = buildLiveChamber(batchId, thresholds);
        List<LeakageMachineDto.TrendPoint> trendData = buildTrendData(testedResults);
        List<LeakageMachineDto.QueueItem> queue = buildQueue(readyJobs, testedResults);
        List<LeakageMachineDto.TestedRecord> history = buildHistory(testedResults);

        return LeakageMachineDto.MachineState.builder()
                .machineStatus(machineStatus)
                .activeBatch(batchId)
                .fileName(thresholds.fileName)
                .warningThreshold(thresholds.warningThreshold)
                .alarmThreshold(thresholds.alarmThreshold)
                .unit(thresholds.unit)
                .totalEmbossed(totalEmbossed)
                .totalTested(totalTested)
                .passedParts(passedParts)
                .failedParts(failedParts)
                .progressPercent(progressPercent)
                .activeChamber(activeChamber)
                .trendData(trendData)
                .queue(queue)
                .history(history)
                .build();
    }

    // -------------------------------------------------------------------------
    // Control Commands: Start, Pause, Reset
    // -------------------------------------------------------------------------
    @Transactional
    public synchronized LeakageMachineDto.MachineState startTesting() {
        String batchId = resolveActiveBatchId();
        if (batchId == null || "No Active Batch".equalsIgnoreCase(batchId)) {
            log.info("Cannot start Leakage Testing Machine: No active embossed batch available.");
            machineStatus = "IDLE";
            return getMachineState();
        }

        // If all items in current batch were previously tested, clear them for a fresh test run
        List<EmbossingJob> readyJobs = getReadyJobsForBatch(batchId);
        List<LeakageTestResult> testedResults = resultRepository.findByBatchIdOrderByIdAsc(batchId);
        if (!readyJobs.isEmpty() && testedResults.size() >= readyJobs.size()) {
            resultRepository.deleteAll(testedResults);
            passedResultRepository.deleteAll(passedResultRepository.findByBatchIdOrderByIdAsc(batchId));
            failedResultRepository.deleteAll(failedResultRepository.findByBatchIdOrderByIdAsc(batchId));
            List<com.arc.leakagetesting.entity.LeakageMachineReading> usedReadings = readingRepository.findByReadingStatusOrderByIdAsc("Used");
            if (usedReadings != null && !usedReadings.isEmpty()) {
                for (com.arc.leakagetesting.entity.LeakageMachineReading r : usedReadings) {
                    r.setReadingStatus("Available");
                }
                readingRepository.saveAll(usedReadings);
            }
            log.info("Cleared previous test results for batch {} to begin fresh test run", batchId);
        }

        machineStatus = "TESTING";
        log.info("Starting Leakage Testing Machine loop for batch {}...", batchId);

        if (runningTask != null) {
            runningTask.cancel(false);
            runningTask = null;
        }

        runningTask = executor.scheduleAtFixedRate(this::runTestingCycle, 0, 1500, TimeUnit.MILLISECONDS);

        LeakageMachineDto.MachineState state = getMachineState();
        broadcastState(state);
        return state;
    }

    public synchronized LeakageMachineDto.MachineState pauseTesting() {
        machineStatus = "PAUSED";
        log.info("Pausing Leakage Testing Machine loop...");

        if (runningTask != null) {
            runningTask.cancel(false);
            runningTask = null;
        }

        LeakageMachineDto.MachineState state = getMachineState();
        broadcastState(state);
        return state;
    }

    @Transactional
    public synchronized LeakageMachineDto.MachineState resetTesting() {
        pauseTesting();
        machineStatus = "IDLE";
        currentProcessingJobId = null;
        livePressure = null;

        String batchId = resolveActiveBatchId();
        if (batchId != null) {
            List<LeakageTestResult> results = resultRepository.findByBatchIdOrderByIdAsc(batchId);
            resultRepository.deleteAll(results);
            passedResultRepository.deleteAll(passedResultRepository.findByBatchIdOrderByIdAsc(batchId));
            failedResultRepository.deleteAll(failedResultRepository.findByBatchIdOrderByIdAsc(batchId));
            log.info("Reset leakage test results for batch {}", batchId);
        }

        // Reset consumed database readings back to "Available"
        List<com.arc.leakagetesting.entity.LeakageMachineReading> usedReadings = readingRepository.findByReadingStatusOrderByIdAsc("Used");
        if (usedReadings != null && !usedReadings.isEmpty()) {
            for (com.arc.leakagetesting.entity.LeakageMachineReading r : usedReadings) {
                r.setReadingStatus("Available");
            }
            readingRepository.saveAll(usedReadings);
            log.info("Reset {} consumed leakage machine readings back to Available status.", usedReadings.size());
        }

        LeakageMachineDto.MachineState state = getMachineState();
        broadcastState(state);
        return state;
    }

    // -------------------------------------------------------------------------
    // Sequential Simulation Processor
    // -------------------------------------------------------------------------
    private void runTestingCycle() {
        if (!"TESTING".equals(machineStatus)) {
            return;
        }

        try {
            String batchId = resolveActiveBatchId();
            if (batchId == null) {
                batchId = findNextBatchWithEmbossedItems();
            }

            if (batchId == null) {
                machineStatus = "COMPLETED";
                log.info("All embossed batches completed for leakage testing.");
                broadcastState(getMachineState());
                pauseTesting();
                return;
            }

            activeBatchId = batchId;
            ThresholdConfig thresholds = getThresholdsForBatch(batchId);
            List<EmbossingJob> readyJobs = getReadyJobsForBatch(batchId);
            List<LeakageTestResult> testedResults = resultRepository.findByBatchIdOrderByIdAsc(batchId);
            Set<String> testedSerials = testedResults.stream().map(LeakageTestResult::getSerialNumber)
                    .collect(Collectors.toSet());

            // Find first untested ready job in order
            Optional<EmbossingJob> nextJobOpt = readyJobs.stream()
                    .filter(j -> !testedSerials.contains(j.getSerialNumber()))
                    .findFirst();

            if (nextJobOpt.isEmpty()) {
                String nextBatchId = findNextBatchWithEmbossedItems();
                if (nextBatchId != null && !nextBatchId.equals(batchId)) {
                    log.info("Batch {} leakage testing complete. Advancing automatically to next batch {}", batchId,
                            nextBatchId);
                    activeBatchId = nextBatchId;
                    return;
                } else {
                    machineStatus = "COMPLETED";
                    log.info("Batch {} completed and no further batches waiting.", batchId);
                    pauseTesting();
                    broadcastState(getMachineState());
                    return;
                }
            }

            EmbossingJob jobToTest = nextJobOpt.get();
            currentProcessingJobId = jobToTest.getId();

            Optional<com.arc.leakagetesting.entity.LeakageMachineReading> readingOpt = readingRepository
                    .findFirstBySerialNumberAndPartNumberAndReadingStatusOrderByIdAsc(
                            jobToTest.getSerialNumber(), jobToTest.getPartNumber(), "Available");
            if (readingOpt.isEmpty()) {
                readingOpt = readingRepository.findFirstByBatchIdAndReadingStatusOrderByIdAsc(batchId, "Available");
            }
            if (readingOpt.isEmpty()) {
                readingOpt = readingRepository.findFirstByReadingStatusOrderByIdAsc("Available");
            }

            if (readingOpt.isEmpty()) {
                log.warn("No available measured reading found in database table leakage_machine_readings for part {} / {}. Pausing machine testing.",
                        jobToTest.getPartNumber(), jobToTest.getSerialNumber());
                machineStatus = "PAUSED";
                livePressure = null;
                broadcastState(getMachineState());
                pauseTesting();
                return;
            }

            com.arc.leakagetesting.entity.LeakageMachineReading reading = readingOpt.get();
            reading.setBatchId(batchId);
            reading.setPartNumber(jobToTest.getPartNumber());
            reading.setSerialNumber(jobToTest.getSerialNumber());
            reading.setReadingStatus("Used");
            reading.setUpdatedAt(LocalDateTime.now());
            readingRepository.save(reading);

            double measuredPressure = reading.getMeasuredValue();
            livePressure = measuredPressure;

            double minVac = Math.min(thresholds.warningThreshold != null ? thresholds.warningThreshold : 75.0, thresholds.alarmThreshold != null ? thresholds.alarmThreshold : 80.0);
            double maxVac = Math.max(thresholds.warningThreshold != null ? thresholds.warningThreshold : 75.0, thresholds.alarmThreshold != null ? thresholds.alarmThreshold : 80.0);

            boolean isPass = measuredPressure >= minVac && measuredPressure <= maxVac;
            String status = isPass ? "PASSED" : "FAILED";

            String direction = measuredPressure < minVac ? "down" : (measuredPressure > maxVac ? "up" : "down");
            String attempt = "1/2";
            String action = isPass ? "Passed" : "Pending";

            // Save result to leakage_test_results PostgreSQL table
            LeakageTestResult result = LeakageTestResult.builder()
                    .batchId(batchId)
                    .partNumber(jobToTest.getPartNumber())
                    .serialNumber(jobToTest.getSerialNumber())
                    .pressureValue(measuredPressure)
                    .unit(thresholds.unit)
                    .warningThreshold(thresholds.warningThreshold)
                    .alarmThreshold(thresholds.alarmThreshold)
                    .status(status)
                    .cycleTimeSeconds(12.8)
                    .attempt(attempt)
                    .action(action)
                    .direction(direction)
                    .testedAt(LocalDateTime.now())
                    .build();

            resultRepository.save(result);

            if (isPass) {
                com.arc.leakagetesting.entity.PassedLeakageTestResult pResult = com.arc.leakagetesting.entity.PassedLeakageTestResult.builder()
                        .batchId(batchId)
                        .partNumber(jobToTest.getPartNumber())
                        .serialNumber(jobToTest.getSerialNumber())
                        .pressureValue(measuredPressure)
                        .unit(thresholds.unit)
                        .warningThreshold(thresholds.warningThreshold)
                        .alarmThreshold(thresholds.alarmThreshold)
                        .status("PASSED")
                        .cycleTimeSeconds(12.8)
                        .attempt(attempt)
                        .action(action)
                        .direction(direction)
                        .testedAt(LocalDateTime.now())
                        .build();
                passedResultRepository.save(pResult);
            } else {
                com.arc.leakagetesting.entity.FailedLeakageTestResult fResult = com.arc.leakagetesting.entity.FailedLeakageTestResult.builder()
                        .batchId(batchId)
                        .partNumber(jobToTest.getPartNumber())
                        .serialNumber(jobToTest.getSerialNumber())
                        .pressureValue(measuredPressure)
                        .unit(thresholds.unit)
                        .warningThreshold(thresholds.warningThreshold)
                        .alarmThreshold(thresholds.alarmThreshold)
                        .status("FAILED")
                        .cycleTimeSeconds(12.8)
                        .attempt(attempt)
                        .action(action)
                        .direction(direction)
                        .testedAt(LocalDateTime.now())
                        .build();
                failedResultRepository.save(fResult);
                if (dashboardService != null) {
                    dashboardService.syncLeakageFailuresFromTestResults();
                }
            }

            log.info("Leakage tested part {} / {}: {} kPa [{}]", jobToTest.getPartNumber(), jobToTest.getSerialNumber(),
                    measuredPressure, status);

            // Broadcast live updated state to frontend
            LeakageMachineDto.MachineState state = getMachineState();
            broadcastState(state);

        } catch (Throwable t) {
            log.error("Error in Leakage Testing cycle: {}", t.getMessage(), t);
        }
    }

    private void broadcastState(LeakageMachineDto.MachineState state) {
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/leakage-testing", state);
                messagingTemplate.convertAndSend("/topic/leakage-progress", state);
            } catch (Exception err) {
                log.warn("Failed to broadcast WebSocket message: {}", err.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper Methods & Resolvers
    // -------------------------------------------------------------------------
    public String resolveActiveBatchId() {
        if ("TESTING".equals(machineStatus) && activeBatchId != null && !"No Active Batch".equalsIgnoreCase(activeBatchId)) {
            return activeBatchId;
        }

        List<String> distinctBatches = new ArrayList<>();

        // 1. Check completed EmbossingJobs (most recent completed job first)
        List<EmbossingJob> completedJobsDesc = embossingJobRepository
                .findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);
        for (EmbossingJob job : completedJobsDesc) {
            String bId = job.getBatchId();
            if (bId != null && !bId.isBlank() && !"No Active Batch".equalsIgnoreCase(bId) && !distinctBatches.contains(bId)) {
                distinctBatches.add(bId);
            }
        }

        // 2. Check completed ProductionBatchItems
        List<ProductionBatchItem> completedItems = productionItemRepository.findByStatusIgnoreCaseOrderByIdAsc("COMPLETED");
        for (int i = completedItems.size() - 1; i >= 0; i--) {
            ProductionBatchItem item = completedItems.get(i);
            String bId = item.getProductionBatch() != null ? item.getProductionBatch().getBatchId() : item.getBatchId();
            if (bId != null && !bId.isBlank() && !"No Active Batch".equalsIgnoreCase(bId) && !distinctBatches.contains(bId)) {
                distinctBatches.add(bId);
            }
        }

        // Find the first (most recent) batch that has untested embossed items
        for (String bId : distinctBatches) {
            long totalBatchEmbossed = getReadyJobsForBatch(bId).size();
            long totalBatchTested = resultRepository.countByBatchId(bId);
            if (totalBatchTested < totalBatchEmbossed) {
                activeBatchId = bId;
                return bId;
            }
        }

        // If all embossed batches are fully tested, return the LATEST embossed batch ID
        if (!distinctBatches.isEmpty()) {
            activeBatchId = distinctBatches.get(0);
            return distinctBatches.get(0);
        }

        activeBatchId = null;
        return null;
    }

    private String findNextBatchWithEmbossedItems() {
        List<EmbossingJob> completedJobsDesc = embossingJobRepository
                .findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);
        List<String> distinctBatches = completedJobsDesc.stream()
                .map(EmbossingJob::getBatchId)
                .filter(b -> b != null && !b.isBlank() && !"No Active Batch".equalsIgnoreCase(b))
                .distinct()
                .collect(Collectors.toList());
        for (String batchId : distinctBatches) {
            long embossedCount = embossingJobRepository
                    .findByBatchIdAndEmbossingStatusOrderByIdAsc(batchId, EmbossingStatus.COMPLETED).size();
            long testedCount = resultRepository.countByBatchId(batchId);
            if (testedCount < embossedCount) {
                return batchId;
            }
        }
        return null;
    }

    private List<EmbossingJob> getReadyJobsForBatch(String batchId) {
        if (batchId == null || "No Active Batch".equalsIgnoreCase(batchId))
            return List.of();

        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdAndEmbossingStatusOrderByIdAsc(batchId,
                EmbossingStatus.COMPLETED);

        List<ProductionBatchItem> items = productionItemRepository
                .findByProductionBatchBatchIdOrderByItemIndexAsc(batchId);
        List<ProductionBatchItem> completedItems = items != null ? items.stream()
                .filter(i -> "COMPLETED".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList()) : List.of();

        if (jobs.isEmpty() && !completedItems.isEmpty()) {
            return completedItems.stream()
                    .map(i -> EmbossingJob.builder()
                            .id(i.getId())
                            .batchId(batchId)
                            .partNumber(i.getPartNumber())
                            .serialNumber(i.getSerialNumber())
                            .embossingStatus(EmbossingStatus.COMPLETED)
                            .build())
                    .collect(Collectors.toList());
        }

        Set<String> jobSerials = jobs.stream().map(EmbossingJob::getSerialNumber).collect(Collectors.toSet());
        List<EmbossingJob> combined = new ArrayList<>(jobs);
        for (ProductionBatchItem item : completedItems) {
            if (!jobSerials.contains(item.getSerialNumber())) {
                combined.add(EmbossingJob.builder()
                        .id(item.getId())
                        .batchId(batchId)
                        .partNumber(item.getPartNumber())
                        .serialNumber(item.getSerialNumber())
                        .embossingStatus(EmbossingStatus.COMPLETED)
                        .build());
            }
        }

        return combined;
    }

    private ThresholdConfig getThresholdsForBatch(String batchId) {
        if (batchId == null || batchId.isBlank() || "No Active Batch".equalsIgnoreCase(batchId)) {
            return new ThresholdConfig("No File", null, null, null);
        }

        String fileName = batchId + ".csv";
        List<SourceDocument> docs = sourceDocumentRepository.findByBatchId(batchId);
        if (docs != null && !docs.isEmpty()) {
            SourceDocument doc = docs.get(docs.size() - 1);
            Double warning = parseDoubleOrDefault(doc.getWarningThreshold(), parseDoubleOrDefault(doc.getMinimumVacuum(), 75.0));
            Double alarm = parseDoubleOrDefault(doc.getAlarmThreshold(), parseDoubleOrDefault(doc.getMaximumVacuum(), 80.0));
            return new ThresholdConfig(fileName, warning, alarm, "kPa");
        }

        return new ThresholdConfig(fileName, 75.0, 80.0, "kPa");
    }

    private Double parseDoubleOrDefault(String raw, Double fallback) {
        if (raw == null || raw.isBlank())
            return fallback;
        try {
            Matcher m = NUMERIC_PATTERN.matcher(raw);
            if (m.find()) {
                return Math.abs(Double.parseDouble(m.group()));
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private LeakageMachineDto.LiveChamber buildLiveChamber(String batchId, ThresholdConfig thresholds) {
        if (currentProcessingJobId == null) {
            return LeakageMachineDto.LiveChamber.builder()
                    .batchId(batchId)
                    .partNumber("-")
                    .serialNumber("-")
                    .currentPressure(null)
                    .unit(thresholds.unit)
                    .warningThreshold(thresholds.warningThreshold)
                    .alarmThreshold(thresholds.alarmThreshold)
                    .status("WAITING")
                    .cycleTimeSeconds(12.8)
                    .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                    .pressureReadings(List.of())
                    .build();
        }

        EmbossingJob job = embossingJobRepository.findById(currentProcessingJobId).orElse(null);
        if (job == null) {
            return LeakageMachineDto.LiveChamber.builder()
                    .batchId(batchId)
                    .partNumber("-")
                    .serialNumber("-")
                    .currentPressure(livePressure)
                    .unit(thresholds.unit)
                    .warningThreshold(thresholds.warningThreshold)
                    .alarmThreshold(thresholds.alarmThreshold)
                    .status("WAITING")
                    .cycleTimeSeconds(12.8)
                    .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                    .build();
        }

        String status = "TESTING";
        if (livePressure != null) {
            boolean isPass = livePressure >= thresholds.warningThreshold && livePressure <= thresholds.alarmThreshold;
            status = isPass ? "PASSED" : "FAILED";
        }

        return LeakageMachineDto.LiveChamber.builder()
                .batchId(batchId)
                .partNumber(job.getPartNumber())
                .serialNumber(job.getSerialNumber())
                .currentPressure(livePressure)
                .unit(thresholds.unit)
                .warningThreshold(thresholds.warningThreshold)
                .alarmThreshold(thresholds.alarmThreshold)
                .status(status)
                .cycleTimeSeconds(12.8)
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();
    }

    private List<LeakageMachineDto.TrendPoint> buildTrendData(List<LeakageTestResult> results) {
        return results.stream()
                .map(r -> LeakageMachineDto.TrendPoint.builder()
                        .serialNumber(r.getSerialNumber())
                        .partNumber(r.getPartNumber())
                        .pressureValue(r.getPressureValue())
                        .passed("PASSED".equalsIgnoreCase(r.getStatus()))
                        .timestamp(r.getTestedAt() != null ? r.getTestedAt().format(TIME_FORMATTER) : "-")
                        .build())
                .collect(Collectors.toList());
    }

    private List<LeakageMachineDto.QueueItem> buildQueue(List<EmbossingJob> readyJobs,
            List<LeakageTestResult> testedResults) {
        Set<String> testedSerials = testedResults.stream().map(LeakageTestResult::getSerialNumber)
                .collect(Collectors.toSet());
        return readyJobs.stream()
                .map(j -> LeakageMachineDto.QueueItem.builder()
                        .id(j.getId())
                        .batchId(j.getBatchId())
                        .partNumber(j.getPartNumber())
                        .serialNumber(j.getSerialNumber())
                        .status(testedSerials.contains(j.getSerialNumber()) ? "COMPLETED" : "READY")
                        .build())
                .collect(Collectors.toList());
    }

    private List<LeakageMachineDto.TestedRecord> buildHistory(List<LeakageTestResult> testedResults) {
        return testedResults.stream()
                .map(r -> LeakageMachineDto.TestedRecord.builder()
                        .id(r.getId())
                        .batchId(r.getBatchId())
                        .partNumber(r.getPartNumber())
                        .serialNumber(r.getSerialNumber())
                        .pressureValue(r.getPressureValue())
                        .unit(r.getUnit())
                        .status(r.getStatus())
                        .timestamp(r.getTestedAt() != null ? r.getTestedAt().format(DATE_TIME_FORMATTER) : "-")
                        .build())
                .collect(Collectors.toList());
    }

    private static class ThresholdConfig {
        final String fileName;
        final Double warningThreshold;
        final Double alarmThreshold;
        final String unit;

        ThresholdConfig(String fileName, Double warningThreshold, Double alarmThreshold, String unit) {
            this.fileName = fileName;
            this.warningThreshold = warningThreshold;
            this.alarmThreshold = alarmThreshold;
            this.unit = unit;
        }
    }
}
