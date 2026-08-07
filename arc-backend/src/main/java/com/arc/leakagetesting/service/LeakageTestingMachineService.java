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
    private final SimpMessagingTemplate messagingTemplate;

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
            @Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.embossingJobRepository = embossingJobRepository;
        this.productionItemRepository = productionItemRepository;
        this.productionBatchRepository = productionBatchRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.resultRepository = resultRepository;
        this.passedResultRepository = passedResultRepository;
        this.failedResultRepository = failedResultRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // -------------------------------------------------------------------------
    // Machine State & Dashboard Aggregation
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public LeakageMachineDto.MachineState getMachineState() {
        String batchId = resolveActiveBatchId();
        ThresholdConfig thresholds = getThresholdsForBatch(batchId);

        List<EmbossingJob> readyJobs = getReadyJobsForBatch(batchId);
        List<LeakageTestResult> testedResults = batchId != null
                ? resultRepository.findByBatchIdOrderByIdAsc(batchId)
                : List.of();

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
                .activeBatch(batchId != null ? batchId : "No Active Batch")
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
    public synchronized LeakageMachineDto.MachineState startTesting() {
        if ("TESTING".equals(machineStatus)) {
            return getMachineState();
        }

        machineStatus = "TESTING";
        log.info("Starting Leakage Testing Machine loop...");

        if (runningTask == null || runningTask.isDone()) {
            runningTask = executor.scheduleAtFixedRate(this::runTestingCycle, 0, 1500, TimeUnit.MILLISECONDS);
        }

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
                // Try next batch if current completed
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
                // Current batch completed! Look for next embossing-completed batch
                // automatically
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

            // Evaluate test pressure reading within DBL range (75.0 - 80.0 kPa)
            double simulatedPressure;
            boolean pass = ThreadLocalRandom.current().nextDouble() > 0.15;
            double minVac = Math.min(thresholds.warningThreshold != null ? thresholds.warningThreshold : 75.0, thresholds.alarmThreshold != null ? thresholds.alarmThreshold : 80.0);
            double maxVac = Math.max(thresholds.warningThreshold != null ? thresholds.warningThreshold : 75.0, thresholds.alarmThreshold != null ? thresholds.alarmThreshold : 80.0);
            if (pass) {
                simulatedPressure = Math.round(
                        (minVac + ThreadLocalRandom.current().nextDouble() * (maxVac - minVac)) * 10.0) / 10.0;
            } else {
                simulatedPressure = Math
                        .round((maxVac + 0.5 + ThreadLocalRandom.current().nextDouble() * 4.5) * 10.0) / 10.0;
            }

            livePressure = simulatedPressure;
            boolean isPass = simulatedPressure >= minVac && simulatedPressure <= maxVac;
            String status = isPass ? "PASSED" : "FAILED";

            String direction = simulatedPressure < minVac ? "down" : (simulatedPressure > maxVac ? "up" : "down");
            String attempt = "1/2";
            String action = isPass ? "Passed" : "Pending";

            // Save result to PostgreSQL table
            LeakageTestResult result = LeakageTestResult.builder()
                    .batchId(batchId)
                    .partNumber(jobToTest.getPartNumber())
                    .serialNumber(jobToTest.getSerialNumber())
                    .pressureValue(simulatedPressure)
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
                        .pressureValue(simulatedPressure)
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
                        .pressureValue(simulatedPressure)
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
            }

            log.info("Leakage tested part {} / {}: {} kPa [{}]", jobToTest.getPartNumber(), jobToTest.getSerialNumber(),
                    simulatedPressure, status);

            // Broadcast live updated state to frontend
            LeakageMachineDto.MachineState state = getMachineState();
            broadcastState(state);

        } catch (Exception e) {
            log.error("Error in Leakage Testing simulation cycle: {}", e.getMessage(), e);
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
        if (activeBatchId != null) {
            return activeBatchId;
        }

        // 1. Get all completed embossing jobs ordered by most recent job ID desc
        List<EmbossingJob> completedJobsDesc = embossingJobRepository
                .findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);

        List<String> distinctBatches = completedJobsDesc.stream()
                .map(EmbossingJob::getBatchId)
                .filter(b -> b != null && !b.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // Find the first (most recent) batch that has untested embossed items
        for (String bId : distinctBatches) {
            long totalBatchEmbossed = embossingJobRepository
                    .findByBatchIdAndEmbossingStatusOrderByIdAsc(bId, EmbossingStatus.COMPLETED).size();
            long totalBatchTested = resultRepository.countByBatchId(bId);
            if (totalBatchTested < totalBatchEmbossed) {
                activeBatchId = bId;
                return bId;
            }
        }

        // 2. If all embossed batches are fully tested, return the LATEST embossed batch ID
        if (!distinctBatches.isEmpty()) {
            activeBatchId = distinctBatches.get(0);
            return distinctBatches.get(0);
        }

        // 3. Fallback to latest production batch created in Data Preparation
        List<ProductionBatch> batches = productionBatchRepository.findAllByOrderByCreatedAtDesc();
        if (!batches.isEmpty()) {
            activeBatchId = batches.get(0).getBatchId();
            return batches.get(0).getBatchId();
        }

        activeBatchId = null;
        return null;
    }

    private String findNextBatchWithEmbossedItems() {
        List<EmbossingJob> completedJobsDesc = embossingJobRepository
                .findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);
        List<String> distinctBatches = completedJobsDesc.stream()
                .map(EmbossingJob::getBatchId)
                .filter(b -> b != null && !b.isBlank())
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
        if (batchId == null)
            return List.of();

        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdAndEmbossingStatusOrderByIdAsc(batchId,
                EmbossingStatus.COMPLETED);
        if (jobs.isEmpty()) {
            // Fallback to production batch items marked COMPLETED
            List<ProductionBatchItem> items = productionItemRepository
                    .findByProductionBatchBatchIdOrderByItemIndexAsc(batchId);
            return items.stream()
                    .filter(i -> "COMPLETED".equalsIgnoreCase(i.getStatus()))
                    .map(i -> EmbossingJob.builder()
                            .id(i.getId())
                            .batchId(batchId)
                            .partNumber(i.getPartNumber())
                            .serialNumber(i.getSerialNumber())
                            .embossingStatus(EmbossingStatus.COMPLETED)
                            .build())
                    .collect(Collectors.toList());
        }
        return jobs;
    }

    private ThresholdConfig getThresholdsForBatch(String batchId) {
        String fileName = (batchId != null && !batchId.isBlank() ? batchId : "Batch") + ".csv";
        return new ThresholdConfig(fileName, 80.0, 75.0, "kPa");
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
