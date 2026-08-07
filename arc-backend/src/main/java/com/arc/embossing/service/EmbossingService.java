package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatch;
import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.dto.BatchProgressResponse;
import com.arc.embossing.dto.CurrentMachineResponse;
import com.arc.embossing.dto.EmbossingDashboardResponse;
import com.arc.embossing.dto.EmbossingJobResponse;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.mapper.EmbossingJobMapper;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EmbossingService {

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingJobMapper embossingJobMapper;
    private final EmbossingSimulationProperties simulationProperties;
    private final ProductionBatchItemRepository productionBatchItemRepository;
    private final EmbossingQueueRepository embossingQueueRepository;
    private final EmbossingDataInitializer embossingDataInitializer;
    private final ProductionBatchRepository productionBatchRepository;

    public EmbossingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingJobMapper embossingJobMapper,
            EmbossingSimulationProperties simulationProperties,
            ProductionBatchItemRepository productionBatchItemRepository,
            EmbossingQueueRepository embossingQueueRepository,
            EmbossingDataInitializer embossingDataInitializer) {
        this(embossingJobRepository, embossingJobMapper, simulationProperties,
             productionBatchItemRepository, embossingQueueRepository, embossingDataInitializer, null);
    }

    @Autowired
    public EmbossingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingJobMapper embossingJobMapper,
            EmbossingSimulationProperties simulationProperties,
            ProductionBatchItemRepository productionBatchItemRepository,
            EmbossingQueueRepository embossingQueueRepository,
            EmbossingDataInitializer embossingDataInitializer,
            ProductionBatchRepository productionBatchRepository) {
        this.embossingJobRepository = embossingJobRepository;
        this.embossingJobMapper = embossingJobMapper;
        this.simulationProperties = simulationProperties;
        this.productionBatchItemRepository = productionBatchItemRepository;
        this.embossingQueueRepository = embossingQueueRepository;
        this.embossingDataInitializer = embossingDataInitializer;
        this.productionBatchRepository = productionBatchRepository;
    }

    @Transactional
    public EmbossingDashboardResponse getDashboard() {
        ensureEmbossingJobsAreSynced();
        String activeBatch = resolveActiveBatchId();
        if (activeBatch == null) {
            return EmbossingDashboardResponse.builder()
                    .activeBatch(null)
                    .pendingCount(0L)
                    .batchProgress(List.of())
                    .jobs(List.of())
                    .build();
        }

        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdOrderByIdAsc(activeBatch);
        List<BatchProgressResponse> batchProgress = buildBatchProgressForActiveBatch(activeBatch);

        long pendingCount = 0;
        if (batchProgress != null && !batchProgress.isEmpty()) {
            List<ProductionBatchItem> batchItems = productionBatchItemRepository != null
                    ? productionBatchItemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(activeBatch)
                    : List.of();
            long completedFromJobs = jobs.stream()
                    .filter(j -> j.getEmbossingStatus() == EmbossingStatus.COMPLETED)
                    .count();
            long completedFromItems = batchItems != null ? batchItems.stream()
                    .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                    .count() : 0;
            int totalFromItems = batchItems != null ? batchItems.size() : 0;
            int totalFromJobs = jobs.size();
            int totalRecords = Math.max(totalFromItems, totalFromJobs);
            long completedRecords = Math.max(completedFromJobs, completedFromItems);
            pendingCount = Math.max(0, totalRecords - completedRecords);
        } else {
            pendingCount = jobs.stream()
                    .filter(job -> job.getEmbossingStatus() == EmbossingStatus.PENDING
                                || job.getEmbossingStatus() == EmbossingStatus.PRINTING
                                || job.getEmbossingStatus() == EmbossingStatus.IN_MACHINE)
                    .count();
        }

        return EmbossingDashboardResponse.builder()
                .activeBatch(activeBatch)
                .pendingCount(pendingCount)
                .batchProgress(batchProgress)
                .jobs(embossingJobMapper.toResponseList(jobs))
                .build();
    }

    @Transactional
    public CurrentMachineResponse getCurrentMachineJob() {
        ensureEmbossingJobsAreSynced();
        return embossingJobRepository
                .findFirstByEmbossingStatusInOrderByIdAsc(
                        List.of(EmbossingStatus.IN_MACHINE, EmbossingStatus.PRINTING))
                .map(this::toCurrentMachineResponse)
                .orElseGet(() -> CurrentMachineResponse.builder()
                        .partNumber(null)
                        .serialNumber(null)
                        .machineStatus(MachineStatus.WAITING)
                        .build());
    }

    @Transactional
    public List<EmbossingJobResponse> getPendingJobs() {
        ensureEmbossingJobsAreSynced();
        List<EmbossingJob> pendingJobs = embossingJobRepository
                .findByEmbossingStatusOrderByIdAsc(EmbossingStatus.PENDING);
        return embossingJobMapper.toResponseList(pendingJobs);
    }

    @Transactional
    public List<EmbossingJobResponse> getCompletedJobs() {
        return getCompletedJobs(null);
    }

    @Transactional(readOnly = true)
    public List<EmbossingJobResponse> getCompletedJobs(String batchId) {
        ensureEmbossingJobsAreSynced();
        String targetBatch = (batchId != null && !batchId.isBlank()) ? batchId : resolveActiveBatchId();
        if (targetBatch == null || targetBatch.isBlank()) {
            return List.of();
        }
        List<EmbossingJob> completedJobs = embossingJobRepository != null
                ? embossingJobRepository.findByBatchIdAndEmbossingStatusOrderByIdAsc(targetBatch, EmbossingStatus.COMPLETED)
                : List.of();
        return embossingJobMapper.toResponseList(completedJobs);
    }

    public List<BatchProgressResponse> buildBatchProgressForActiveBatch(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            return List.of();
        }
        List<ProductionBatchItem> batchItems = productionBatchItemRepository != null
                ? productionBatchItemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(batchId)
                : List.of();
        if (batchItems.isEmpty()) {
            List<EmbossingJob> jobs = embossingJobRepository != null
                    ? embossingJobRepository.findByBatchIdOrderByIdAsc(batchId)
                    : List.of();
            if (jobs.isEmpty()) {
                return List.of(buildBatchProgress(batchId, List.of()));
            }
            return List.of(buildBatchProgress(batchId, List.of()));
        }

        return List.of(buildBatchProgress(batchId, batchItems));
    }

    public BatchProgressResponse buildBatchProgress(String batchId, List<ProductionBatchItem> items) {
        List<EmbossingJob> jobs = embossingJobRepository != null
                ? embossingJobRepository.findByBatchIdOrderByIdAsc(batchId)
                : List.of();

        long completedFromJobs = jobs.stream()
                .filter(j -> j.getEmbossingStatus() == EmbossingStatus.COMPLETED)
                .count();
        long completedFromItems = items != null ? items.stream()
                .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                .count() : 0;

        int totalFromItems = items != null ? items.size() : 0;
        int totalFromJobs = jobs.size();

        int totalRecords = Math.max(totalFromItems, totalFromJobs);
        long completedRecords = Math.max(completedFromJobs, completedFromItems);
        long pendingRecords = Math.max(0, totalRecords - completedRecords);
        int progressPercent = totalRecords == 0 ? 0 : (int) Math.round((completedRecords * 100.0) / totalRecords);
        boolean completed = completedRecords >= totalRecords && totalRecords > 0;

        return BatchProgressResponse.builder()
                .batchId(batchId)
                .progressPercent(progressPercent)
                .completed(completed)
                .build();
    }

    private void ensureEmbossingJobsAreSynced() {
        if (embossingDataInitializer != null) {
            embossingDataInitializer.syncEmbossingJobsFromQueue();
        }
    }

    public String resolveActiveBatchId() {
        if (embossingJobRepository != null) {
            Optional<EmbossingJob> activeJob = embossingJobRepository.findFirstByEmbossingStatusInOrderByIdAsc(
                    List.of(EmbossingStatus.IN_MACHINE, EmbossingStatus.PRINTING, EmbossingStatus.PENDING));
            if (activeJob.isPresent() && activeJob.get().getBatchId() != null && !activeJob.get().getBatchId().isBlank()) {
                return activeJob.get().getBatchId();
            }

            Optional<EmbossingJob> latestJob = embossingJobRepository.findFirstByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED);
            if (latestJob.isPresent() && latestJob.get().getBatchId() != null && !latestJob.get().getBatchId().isBlank()) {
                return latestJob.get().getBatchId();
            }

            List<EmbossingJob> allJobs = embossingJobRepository.findAll();
            if (!allJobs.isEmpty()) {
                return allJobs.get(allJobs.size() - 1).getBatchId();
            }
        }
        if (productionBatchRepository != null) {
            List<ProductionBatch> batches = productionBatchRepository.findAll();
            if (!batches.isEmpty()) {
                return batches.get(batches.size() - 1).getBatchId();
            }
        }
        if (simulationProperties != null && simulationProperties.getActiveBatch() != null && !simulationProperties.getActiveBatch().isBlank()) {
            return simulationProperties.getActiveBatch();
        }
        return "Batch_1";
    }

    private CurrentMachineResponse toCurrentMachineResponse(EmbossingJob job) {
        MachineStatus displayStatus = job.getEmbossingStatus() == EmbossingStatus.IN_MACHINE
                ? MachineStatus.IN_MACHINE
                : MachineStatus.PRINTING;

        return CurrentMachineResponse.builder()
                .partNumber(job.getPartNumber())
                .serialNumber(job.getSerialNumber())
                .machineStatus(displayStatus)
                .build();
    }
}
