package com.arc.leakagetesting.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import com.arc.leakagetesting.entity.FailedLeakageTestResult;
import com.arc.leakagetesting.entity.LeakageTestResult;
import com.arc.leakagetesting.entity.PassedLeakageTestResult;
import com.arc.leakagetesting.repository.FailedLeakageTestResultRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import com.arc.leakagetesting.repository.PassedLeakageTestResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeakageTestingService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd MMM");
    private static final DateTimeFormatter DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("d MMMM, yyyy");

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingSimulationProperties simulationProperties;
    private final LeakageTestResultRepository resultRepository;
    private final PassedLeakageTestResultRepository passedResultRepository;
    private final FailedLeakageTestResultRepository failedResultRepository;
    private final LeakageTestingMachineService machineService;

    @Autowired
    public LeakageTestingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingSimulationProperties simulationProperties,
            LeakageTestResultRepository resultRepository,
            PassedLeakageTestResultRepository passedResultRepository,
            FailedLeakageTestResultRepository failedResultRepository,
            @Autowired(required = false) LeakageTestingMachineService machineService) {
        this.embossingJobRepository = embossingJobRepository;
        this.simulationProperties = simulationProperties;
        this.resultRepository = resultRepository;
        this.passedResultRepository = passedResultRepository;
        this.failedResultRepository = failedResultRepository;
        this.machineService = machineService;
    }

    @Transactional(readOnly = true)
    public LeakageTestingResponseDto getDashboardData() {
        String activeBatch = resolveActiveBatchId();

        if (activeBatch == null || activeBatch.isBlank() || "No Active Batch".equalsIgnoreCase(activeBatch)) {
            String currentDateDisplay = LocalDate.now().format(DATE_DISPLAY_FORMATTER);
            return LeakageTestingResponseDto.builder()
                    .activeBatch("No Active Batch")
                    .failedCount(0L)
                    .passedCount(0L)
                    .batchProgressPercent(0)
                    .completedCount(0L)
                    .totalParts(0L)
                    .dateDisplay(currentDateDisplay)
                    .batchStatus("No Batch")
                    .failures(new ArrayList<>())
                    .passed(new ArrayList<>())
                    .build();
        }

        List<PassedLeakageTestResult> passedEntities = passedResultRepository.findByBatchIdOrderByIdAsc(activeBatch);
        List<FailedLeakageTestResult> failedEntities = failedResultRepository.findByBatchIdOrderByIdAsc(activeBatch);
        List<LeakageTestResult> allTestResults = resultRepository.findByBatchIdOrderByIdAsc(activeBatch);
        List<EmbossingJob> embossingJobs = embossingJobRepository.findByBatchIdOrderByIdAsc(activeBatch);

        long totalParts = Math.max(embossingJobs.size(), allTestResults.size());

        List<LeakageTestItemDto> failureDtos;
        List<LeakageTestItemDto> passedDtos;

        if (!failedEntities.isEmpty() || !passedEntities.isEmpty()) {
            failureDtos = failedEntities.stream().map(this::toItemDto).collect(Collectors.toList());
            passedDtos = passedEntities.stream().map(this::toItemDto).collect(Collectors.toList());
        } else if (!allTestResults.isEmpty()) {
            failureDtos = new ArrayList<>();
            passedDtos = new ArrayList<>();
            for (LeakageTestResult res : allTestResults) {
                LeakageTestItemDto dto = toItemDto(res);
                if ("FAILED".equalsIgnoreCase(res.getStatus())) {
                    failureDtos.add(dto);
                } else {
                    passedDtos.add(dto);
                }
            }
        } else {
            failureDtos = new ArrayList<>();
            passedDtos = new ArrayList<>();
        }

        long completedCount = failureDtos.size() + passedDtos.size();
        long failedCount = failureDtos.size();
        long passedCount = passedDtos.size();

        int progressPercent = totalParts > 0 ? (int) Math.round((double) completedCount / totalParts * 100) : 0;
        if (progressPercent > 100) {
            progressPercent = 100;
        }

        String batchStatus = (progressPercent >= 100 && totalParts > 0) ? "100% completed" : (totalParts == 0 ? "No Batch" : "In Progress");
        String currentDateDisplay = LocalDate.now().format(DATE_DISPLAY_FORMATTER);

        return LeakageTestingResponseDto.builder()
                .activeBatch(activeBatch)
                .failedCount(failedCount)
                .passedCount(passedCount)
                .batchProgressPercent(progressPercent)
                .completedCount(completedCount)
                .totalParts(totalParts)
                .dateDisplay(currentDateDisplay)
                .batchStatus(batchStatus)
                .failures(failureDtos)
                .passed(passedDtos)
                .build();
    }

    public String resolveActiveBatchId() {
        // 1. Live Active Batch from Leakage Machine
        if (machineService != null) {
            try {
                String active = machineService.resolveActiveBatchId();
                if (active != null && !active.isBlank() && !"No Active Batch".equalsIgnoreCase(active)) {
                    return active;
                }
            } catch (Exception e) {
                log.warn("Could not retrieve active batch from machine service: {}", e.getMessage());
            }
        }

        // 2. Latest completed embossing job batch
        List<EmbossingJob> completedJobs = embossingJobRepository.findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);
        if (!completedJobs.isEmpty()) {
            return completedJobs.get(0).getBatchId();
        }

        // 3. Latest embossing job batch overall
        List<EmbossingJob> allJobs = embossingJobRepository.findAll();
        if (!allJobs.isEmpty()) {
            return allJobs.get(allJobs.size() - 1).getBatchId();
        }

        return "No Active Batch";
    }

    @Transactional
    public LeakageTestItemDto updateJobAction(Long id, String newAction) {
        Optional<FailedLeakageTestResult> failedOpt = failedResultRepository.findById(id);
        if (failedOpt.isPresent()) {
            FailedLeakageTestResult fResult = failedOpt.get();
            fResult.setAction(newAction);
            FailedLeakageTestResult saved = failedResultRepository.save(fResult);
            log.info("Updated action for FailedLeakageTestResult ID {} ({}) to {}", id, saved.getPartNumber(), newAction);
            return toItemDto(saved);
        }

        Optional<LeakageTestResult> resultOpt = resultRepository.findById(id);
        if (resultOpt.isPresent()) {
            LeakageTestResult result = resultOpt.get();
            result.setAction(newAction);
            LeakageTestResult saved = resultRepository.save(result);
            log.info("Updated action for LeakageTestResult ID {} ({}) to {}", id, saved.getPartNumber(), newAction);
            return toItemDto(saved);
        }

        EmbossingJob job = embossingJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leakage test record or embossing job not found with id: " + id));

        log.info("Requested action update for EmbossingJob ID {} ({}) to {}", id, job.getPartNumber(), newAction);
        return toItemDto(job, "Failed");
    }

    @Transactional
    public LeakageTestItemDto markJobAsFailed(Long jobId, Double testValue, String direction, String attempt, String action) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Embossing job not found with id: " + jobId));

        job.setEmbossingStatus(EmbossingStatus.FAILED);
        job.setEmbossingCompletedTime(LocalDateTime.now());
        embossingJobRepository.save(job);

        FailedLeakageTestResult fResult = FailedLeakageTestResult.builder()
                .batchId(job.getBatchId())
                .partNumber(job.getPartNumber())
                .serialNumber(job.getSerialNumber())
                .pressureValue(testValue != null ? testValue : 82.5)
                .unit("kPa")
                .status("FAILED")
                .direction(direction != null ? direction : "up")
                .attempt(attempt != null ? attempt : "1/2")
                .action(action != null ? action : "Pending")
                .testedAt(LocalDateTime.now())
                .build();

        FailedLeakageTestResult saved = failedResultRepository.save(fResult);
        log.info("Marked job ID {} ({}) as FAILED", jobId, job.getPartNumber());
        return toItemDto(saved);
    }

    private LeakageTestItemDto toItemDto(PassedLeakageTestResult result) {
        LocalDateTime time = result.getTestedAt() != null ? result.getTestedAt() : LocalDateTime.now();
        String formattedTime = time.format(TIMESTAMP_FORMATTER);

        return LeakageTestItemDto.builder()
                .id(result.getId())
                .partNo(result.getPartNumber())
                .serialNo(result.getSerialNumber())
                .status("Passed")
                .testValue(result.getPressureValue() != null ? result.getPressureValue() : 78.5)
                .direction(result.getDirection() != null ? result.getDirection() : "up")
                .timestamp(formattedTime)
                .attempt(result.getAttempt() != null ? result.getAttempt() : "1/2")
                .action(result.getAction() != null ? result.getAction() : "Passed")
                .build();
    }

    private LeakageTestItemDto toItemDto(FailedLeakageTestResult result) {
        LocalDateTime time = result.getTestedAt() != null ? result.getTestedAt() : LocalDateTime.now();
        String formattedTime = time.format(TIMESTAMP_FORMATTER);

        return LeakageTestItemDto.builder()
                .id(result.getId())
                .partNo(result.getPartNumber())
                .serialNo(result.getSerialNumber())
                .status("Failed")
                .testValue(result.getPressureValue() != null ? result.getPressureValue() : 82.5)
                .direction(result.getDirection() != null ? result.getDirection() : "down")
                .timestamp(formattedTime)
                .attempt(result.getAttempt() != null ? result.getAttempt() : "1/2")
                .action(result.getAction() != null ? result.getAction() : "Pending")
                .build();
    }

    private LeakageTestItemDto toItemDto(LeakageTestResult result) {
        LocalDateTime time = result.getTestedAt() != null ? result.getTestedAt() : LocalDateTime.now();
        String formattedTime = time.format(TIMESTAMP_FORMATTER);

        String statusDisplay = "PASSED".equalsIgnoreCase(result.getStatus()) ? "Passed" : "Failed";
        String direction = result.getDirection() != null ? result.getDirection() : ("Passed".equalsIgnoreCase(statusDisplay) ? "up" : "down");
        String attempt = result.getAttempt() != null ? result.getAttempt() : "1/2";
        String action = result.getAction() != null ? result.getAction() : ("Passed".equalsIgnoreCase(statusDisplay) ? "Passed" : "Pending");

        return LeakageTestItemDto.builder()
                .id(result.getId())
                .partNo(result.getPartNumber())
                .serialNo(result.getSerialNumber())
                .status(statusDisplay)
                .testValue(result.getPressureValue() != null ? result.getPressureValue() : 78.5)
                .direction(direction)
                .timestamp(formattedTime)
                .attempt(attempt)
                .action(action)
                .build();
    }

    private LeakageTestItemDto toItemDto(EmbossingJob job, String statusDisplay) {
        LocalDateTime time = job.getEmbossingCompletedTime() != null ? job.getEmbossingCompletedTime() : job.getCreatedTime();
        String formattedTime = time != null ? time.format(TIMESTAMP_FORMATTER) : LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        return LeakageTestItemDto.builder()
                .id(job.getId())
                .partNo(job.getPartNumber())
                .serialNo(job.getSerialNumber())
                .status(statusDisplay)
                .testValue("Passed".equalsIgnoreCase(statusDisplay) ? 77.4 : 82.5)
                .direction("Passed".equalsIgnoreCase(statusDisplay) ? "up" : "down")
                .timestamp(formattedTime)
                .attempt("1/2")
                .action("Passed".equalsIgnoreCase(statusDisplay) ? "Passed" : "Pending")
                .build();
    }
}
