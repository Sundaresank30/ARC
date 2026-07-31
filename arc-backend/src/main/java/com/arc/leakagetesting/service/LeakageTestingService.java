package com.arc.leakagetesting.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeakageTestingService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd MMM");

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingSimulationProperties simulationProperties;

    public LeakageTestingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingSimulationProperties simulationProperties) {
        this.embossingJobRepository = embossingJobRepository;
        this.simulationProperties = simulationProperties;
    }

    @Transactional(readOnly = true)
    public LeakageTestingResponseDto getDashboardData() {
        String activeBatch = simulationProperties.getActiveBatch();
        if (activeBatch == null || activeBatch.isEmpty()) {
            activeBatch = "Batch_1";
        }

        List<EmbossingJob> allJobs = embossingJobRepository.findByBatchIdOrderByIdAsc(activeBatch);
        long totalParts = allJobs.size();

        // Completed or Failed jobs count towards processed total
        long processedCount = allJobs.stream()
                .filter(j -> j.getEmbossingStatus() == EmbossingStatus.COMPLETED || j.getEmbossingStatus() == EmbossingStatus.FAILED)
                .count();

        // ONLY jobs with FAILED status in embossing_jobs are shown in the inspection table
        List<EmbossingJob> failedJobs = allJobs.stream()
                .filter(j -> j.getEmbossingStatus() == EmbossingStatus.FAILED)
                .collect(Collectors.toList());

        long failedCount = failedJobs.size();

        int progressPercent = totalParts > 0 ? (int) Math.round((double) processedCount / totalParts * 100) : 0;

        List<LeakageTestItemDto> failureDtos = failedJobs.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return LeakageTestingResponseDto.builder()
                .activeBatch(activeBatch)
                .failedCount(failedCount)
                .batchProgressPercent(progressPercent)
                .completedCount(processedCount)
                .totalParts(totalParts)
                .dateDisplay("20 July, 2026")
                .failures(failureDtos)
                .build();
    }

    @Transactional
    public LeakageTestItemDto updateJobAction(Long jobId, String newAction) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Embossing job not found with id: " + jobId));

        job.setAction(newAction);
        EmbossingJob saved = embossingJobRepository.save(job);
        log.info("Updated action for job ID {} ({}) to {}", jobId, saved.getPartNumber(), newAction);
        return toItemDto(saved);
    }

    @Transactional
    public LeakageTestItemDto markJobAsFailed(Long jobId, Double testValue, String direction, String attempt, String action) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Embossing job not found with id: " + jobId));

        job.setEmbossingStatus(EmbossingStatus.FAILED);
        job.setTestValue(testValue != null ? testValue : 0.42);
        job.setDirection(direction != null ? direction : "down");
        job.setAttempt(attempt != null ? attempt : "1/2");
        job.setAction(action != null ? action : "Pending");
        job.setEmbossingCompletedTime(LocalDateTime.now());

        EmbossingJob saved = embossingJobRepository.save(job);
        log.info("Marked job ID {} ({}) as FAILED with test value {}", jobId, saved.getPartNumber(), testValue);
        return toItemDto(saved);
    }

    private LeakageTestItemDto toItemDto(EmbossingJob job) {
        LocalDateTime time = job.getEmbossingCompletedTime() != null ? job.getEmbossingCompletedTime() : job.getCreatedTime();
        String formattedTime = time != null ? time.format(TIMESTAMP_FORMATTER) : "17:57, 20 Jul";

        return LeakageTestItemDto.builder()
                .id(job.getId())
                .partNo(job.getPartNumber())
                .serialNo(job.getSerialNumber())
                .status("Failed")
                .testValue(job.getTestValue() != null ? job.getTestValue() : 0.42)
                .direction(job.getDirection() != null ? job.getDirection() : "down")
                .timestamp(formattedTime)
                .attempt(job.getAttempt() != null ? job.getAttempt() : "1/2")
                .action(job.getAction() != null ? job.getAction() : "Pending")
                .build();
    }
}
