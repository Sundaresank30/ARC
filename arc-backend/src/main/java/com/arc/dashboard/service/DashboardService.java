package com.arc.dashboard.service;

import com.arc.dashboard.dto.CarryForwardDTO;
import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.dto.LeakageFailureDTO;
import com.arc.dashboard.entity.CarryForwardEmbossing;
import com.arc.dashboard.entity.LeakageFailure;
import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.dashboard.repository.LeakageFailureRepository;
import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.entity.FailedLeakageTestResult;
import com.arc.leakagetesting.repository.FailedLeakageTestResultRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd MMM");

    private final CarryForwardEmbossingRepository carryForwardRepository;
    private final LeakageFailureRepository leakageFailureRepository;
    private final ProductionBatchRepository productionBatchRepository;
    private final EmbossingJobRepository embossingJobRepository;
    private final LeakageTestResultRepository leakageTestResultRepository;
    private final EmbossingQueueRepository embossingQueueRepository;
    private final FailedLeakageTestResultRepository failedLeakageTestResultRepository;
    private final ProductionBatchItemRepository productionBatchItemRepository;

    @Autowired
    public DashboardService(
            CarryForwardEmbossingRepository carryForwardRepository,
            LeakageFailureRepository leakageFailureRepository,
            @Autowired(required = false) ProductionBatchRepository productionBatchRepository,
            @Autowired(required = false) EmbossingJobRepository embossingJobRepository,
            @Autowired(required = false) LeakageTestResultRepository leakageTestResultRepository,
            @Autowired(required = false) EmbossingQueueRepository embossingQueueRepository,
            @Autowired(required = false) FailedLeakageTestResultRepository failedLeakageTestResultRepository,
            @Autowired(required = false) ProductionBatchItemRepository productionBatchItemRepository) {
        this.carryForwardRepository = carryForwardRepository;
        this.leakageFailureRepository = leakageFailureRepository;
        this.productionBatchRepository = productionBatchRepository;
        this.embossingJobRepository = embossingJobRepository;
        this.leakageTestResultRepository = leakageTestResultRepository;
        this.embossingQueueRepository = embossingQueueRepository;
        this.failedLeakageTestResultRepository = failedLeakageTestResultRepository;
        this.productionBatchItemRepository = productionBatchItemRepository;
    }

    /**
     * Synchronize pending records from embossing_queue into carry_foward_embossing.
     * Also remove completed records so carry_foward_embossing contains ONLY active pending items.
     */
    @Transactional
    public void syncCarryForwardFromEmbossingQueue() {
        if (embossingQueueRepository == null) {
            return;
        }

        // 1. Gather all completed keys (partNumber::serialNumber) across all tables
        java.util.Set<String> completedKeys = new java.util.HashSet<>();

        if (embossingJobRepository != null) {
            embossingJobRepository.findByEmbossingStatusOrderByIdAsc(EmbossingStatus.COMPLETED)
                    .forEach(j -> completedKeys.add(j.getPartNumber() + "::" + j.getSerialNumber()));
        }

        if (productionBatchItemRepository != null) {
            productionBatchItemRepository.findAll().stream()
                    .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                    .forEach(item -> completedKeys.add(item.getPartNumber() + "::" + item.getSerialNumber()));
        }

        embossingQueueRepository.findAll().stream()
                .filter(q -> q.getStatus() == EmbossingQueueStatus.COMPLETED)
                .forEach(q -> completedKeys.add(q.getPartNumber() + "::" + q.getSerialNumber()));

        // 2. Immediately remove all completed items from carry_forward_embossing
        for (String key : completedKeys) {
            String[] parts = key.split("::");
            if (parts.length == 2) {
                carryForwardRepository.findByPartNoAndSerialNo(parts[0], parts[1])
                        .ifPresent(carryForwardRepository::delete);
            }
        }

        // 3. Synchronize non-COMPLETED production batch items into embossing_queue (excluding completed keys)
        if (productionBatchItemRepository != null) {
            List<ProductionBatchItem> nonCompletedItems = productionBatchItemRepository.findAll().stream()
                    .filter(item -> item.getStatus() == null || !"COMPLETED".equalsIgnoreCase(item.getStatus()))
                    .filter(item -> !completedKeys.contains(item.getPartNumber() + "::" + item.getSerialNumber()))
                    .toList();
            for (ProductionBatchItem pItem : nonCompletedItems) {
                if (!embossingQueueRepository.existsBySerialNumberAndPartNumber(pItem.getSerialNumber(), pItem.getPartNumber())) {
                    embossingQueueRepository.save(EmbossingQueue.builder()
                            .partNumber(pItem.getPartNumber())
                            .serialNumber(pItem.getSerialNumber())
                            .status(EmbossingQueueStatus.WAITING)
                            .build());
                }
            }
        }

        // 4. Synchronize non-COMPLETED embossing jobs into embossing_queue (excluding completed keys)
        if (embossingJobRepository != null) {
            List<com.arc.embossing.entity.EmbossingJob> nonCompletedJobs = embossingJobRepository.findAll().stream()
                    .filter(j -> j.getEmbossingStatus() != null && j.getEmbossingStatus() != EmbossingStatus.COMPLETED)
                    .filter(j -> !completedKeys.contains(j.getPartNumber() + "::" + j.getSerialNumber()))
                    .toList();
            for (com.arc.embossing.entity.EmbossingJob job : nonCompletedJobs) {
                if (!embossingQueueRepository.existsBySerialNumberAndPartNumber(job.getSerialNumber(), job.getPartNumber())) {
                    embossingQueueRepository.save(EmbossingQueue.builder()
                            .partNumber(job.getPartNumber())
                            .serialNumber(job.getSerialNumber())
                            .status(job.getEmbossingStatus() == EmbossingStatus.PRINTING ? EmbossingQueueStatus.IN_PROGRESS : EmbossingQueueStatus.WAITING)
                            .build());
                }
            }
        }

        // 5. Upsert only non-completed queue items into carry_forward_embossing
        List<EmbossingQueue> pendingQueueItems = embossingQueueRepository.findAll().stream()
                .filter(q -> q.getStatus() != EmbossingQueueStatus.COMPLETED)
                .filter(q -> !completedKeys.contains(q.getPartNumber() + "::" + q.getSerialNumber()))
                .toList();

        for (EmbossingQueue queueItem : pendingQueueItems) {
            Optional<CarryForwardEmbossing> existingOpt = carryForwardRepository
                    .findByPartNoAndSerialNo(queueItem.getPartNumber(), queueItem.getSerialNumber());

            if (existingOpt.isEmpty()) {
                CarryForwardEmbossing newEntry = CarryForwardEmbossing.builder()
                        .partNo(queueItem.getPartNumber())
                        .serialNo(queueItem.getSerialNumber())
                        .status("Pending")
                        .remainingSince(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                        .nextShift("Shift A")
                        .action("Pending")
                        .build();
                carryForwardRepository.save(newEntry);
            }
        }
    }

    /**
     * Synchronize actual failed leakage test results from failed_leakage_test_results into leakage_failures.
     */
    @Transactional
    public void syncLeakageFailuresFromTestResults() {
        if (failedLeakageTestResultRepository == null) {
            return;
        }

        List<FailedLeakageTestResult> failedResults = failedLeakageTestResultRepository.findAll();

        for (FailedLeakageTestResult fResult : failedResults) {
            Optional<LeakageFailure> existingOpt = leakageFailureRepository
                    .findByPartNoAndSerialNo(fResult.getPartNumber(), fResult.getSerialNumber());

            String formattedTime = fResult.getTestedAt() != null
                    ? fResult.getTestedAt().format(TIMESTAMP_FORMATTER)
                    : LocalDateTime.now().format(TIMESTAMP_FORMATTER);

            if (existingOpt.isEmpty()) {
                LeakageFailure failure = LeakageFailure.builder()
                        .partNo(fResult.getPartNumber())
                        .serialNo(fResult.getSerialNumber())
                        .status(fResult.getStatus() != null ? fResult.getStatus() : "Failed")
                        .testValue(fResult.getPressureValue() != null ? fResult.getPressureValue() : 82.5)
                        .direction(fResult.getDirection() != null ? fResult.getDirection() : "down")
                        .timestamp(formattedTime)
                        .attempt(fResult.getAttempt() != null ? fResult.getAttempt() : "1/2")
                        .action(fResult.getAction() != null ? fResult.getAction() : "Pending")
                        .build();
                leakageFailureRepository.save(failure);
            } else {
                LeakageFailure existing = existingOpt.get();
                existing.setTestValue(fResult.getPressureValue() != null ? fResult.getPressureValue() : existing.getTestValue());
                existing.setDirection(fResult.getDirection() != null ? fResult.getDirection() : existing.getDirection());
                existing.setTimestamp(formattedTime);
                if (fResult.getAttempt() != null) existing.setAttempt(fResult.getAttempt());
                if (fResult.getAction() != null) existing.setAction(fResult.getAction());
                leakageFailureRepository.save(existing);
            }
        }
    }

    @Transactional
    public List<CarryForwardDTO> getCarryForwardItems() {
        syncCarryForwardFromEmbossingQueue();
        List<CarryForwardEmbossing> list = carryForwardRepository.findByStatusNotIgnoreCase("Completed");
        return list.stream()
                .map(this::toCarryForwardDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LeakageFailureDTO> getLeakageFailures() {
        syncLeakageFailuresFromTestResults();
        List<LeakageFailure> list = leakageFailureRepository.findAll();
        return list.stream()
                .map(this::toLeakageFailureDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DashboardResponseDTO getDashboardSummary() {
        syncCarryForwardFromEmbossingQueue();
        syncLeakageFailuresFromTestResults();

        List<CarryForwardEmbossing> carryForwardList = carryForwardRepository.findByStatusNotIgnoreCase("Completed");
        List<LeakageFailure> leakageFailureList = leakageFailureRepository.findAll();

        long completedJobsCount = (embossingJobRepository != null)
                ? embossingJobRepository.countByEmbossingStatus(EmbossingStatus.COMPLETED)
                : 0;

        long leakageFailuresCount = (failedLeakageTestResultRepository != null)
                ? failedLeakageTestResultRepository.count()
                : leakageFailureList.size();

        long totalBatchesCount = (productionBatchRepository != null)
                ? productionBatchRepository.count()
                : 0;

        List<CarryForwardDTO> carryForwardDTOs = carryForwardList.stream()
                .map(this::toCarryForwardDTO)
                .collect(Collectors.toList());

        List<LeakageFailureDTO> leakageFailureDTOs = leakageFailureList.stream()
                .map(this::toLeakageFailureDTO)
                .collect(Collectors.toList());

        return DashboardResponseDTO.builder()
                .completedCount((int) completedJobsCount)
                .failedCount((int) leakageFailuresCount)
                .totalBatches((int) totalBatchesCount)
                .carryForwardEmbossing(carryForwardDTOs)
                .leakageTestingFailures(leakageFailureDTOs)
                .build();
    }

    @Transactional
    public void resolveCarryForward(Long id) {
        carryForwardRepository.findById(id).ifPresent(carryForwardRepository::delete);
    }

    @Transactional
    public void resolveLeakageFailure(Long id) {
        leakageFailureRepository.findById(id).ifPresent(item -> {
            item.setStatus("Resolved");
            item.setAction("Resolved");
            leakageFailureRepository.save(item);
        });
    }

    private CarryForwardDTO toCarryForwardDTO(CarryForwardEmbossing item) {
        return CarryForwardDTO.builder()
                .id(String.valueOf(item.getId()))
                .partNo(item.getPartNo())
                .serialNo(item.getSerialNo())
                .status(item.getStatus())
                .remainingSince(item.getRemainingSince())
                .nextShift(item.getNextShift())
                .action(item.getAction())
                .build();
    }

    private LeakageFailureDTO toLeakageFailureDTO(LeakageFailure item) {
        return LeakageFailureDTO.builder()
                .id(String.valueOf(item.getId()))
                .partNo(item.getPartNo())
                .serialNo(item.getSerialNo())
                .status(item.getStatus())
                .testValue(item.getTestValue())
                .direction(item.getDirection())
                .timestamp(item.getTimestamp())
                .attempt(item.getAttempt())
                .action(item.getAction())
                .build();
    }
}
