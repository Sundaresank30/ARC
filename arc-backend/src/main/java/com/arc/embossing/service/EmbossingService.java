package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class EmbossingService {

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingJobMapper embossingJobMapper;
    private final EmbossingSimulationProperties simulationProperties;
    private final ProductionBatchItemRepository productionBatchItemRepository;
    private final EmbossingQueueRepository embossingQueueRepository;
    private final EmbossingDataInitializer embossingDataInitializer;

    public EmbossingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingJobMapper embossingJobMapper,
            EmbossingSimulationProperties simulationProperties,
            ProductionBatchItemRepository productionBatchItemRepository,
            EmbossingQueueRepository embossingQueueRepository,
            EmbossingDataInitializer embossingDataInitializer) {
        this.embossingJobRepository = embossingJobRepository;
        this.embossingJobMapper = embossingJobMapper;
        this.simulationProperties = simulationProperties;
        this.productionBatchItemRepository = productionBatchItemRepository;
        this.embossingQueueRepository = embossingQueueRepository;
        this.embossingDataInitializer = embossingDataInitializer;
    }

    @Transactional
    public EmbossingDashboardResponse getDashboard() {
        ensureEmbossingJobsAreSynced();
        String activeBatch = resolveActiveBatchId();
        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdOrderByIdAsc(activeBatch);
        List<BatchProgressResponse> batchProgress = buildBatchProgressForActiveBatch(activeBatch);

        long pendingCount = 0;
        if (batchProgress != null && !batchProgress.isEmpty()) {
            pendingCount = batchProgress.get(0).getPendingRecords();
        } else {
            pendingCount = jobs.stream().filter(job -> job.getEmbossingStatus() == EmbossingStatus.PENDING).count();
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
        ensureEmbossingJobsAreSynced();
        List<EmbossingJob> completedJobs = embossingJobRepository
                .findByEmbossingStatusOrderByIdAsc(EmbossingStatus.COMPLETED);
        return embossingJobMapper.toResponseList(completedJobs);
    }

    public List<BatchProgressResponse> buildBatchProgressForActiveBatch(String batchId) {
        List<ProductionBatchItem> batchItems = productionBatchItemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(batchId);
        if (batchItems.isEmpty()) {
            List<ProductionBatchItem> fallbackItems = productionBatchItemRepository.findAll();
            if (fallbackItems.isEmpty()) {
                return List.of(buildBatchProgress(batchId, List.of()));
            }
            return List.of(buildBatchProgress(resolveBatchIdFromItem(fallbackItems.get(0)), fallbackItems));
        }

        return List.of(buildBatchProgress(batchId, batchItems));
    }

    public BatchProgressResponse buildBatchProgress(String batchId, List<ProductionBatchItem> items) {
        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdOrderByIdAsc(batchId);
        long completedFromJobs = jobs.stream()
                .filter(j -> j.getEmbossingStatus() == EmbossingStatus.COMPLETED)
                .count();
        long completedFromItems = items.stream()
                .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                .count();
        long completedFromQueue = embossingQueueRepository != null
                ? embossingQueueRepository.countByStatus(EmbossingQueueStatus.COMPLETED)
                : 0;

        int totalFromQueue = (int) (embossingQueueRepository != null ? embossingQueueRepository.count() : 0);
        int totalFromItems = items.size();
        int totalFromJobs = jobs.size();

        int totalRecords = Math.max(totalFromQueue, Math.max(totalFromItems, totalFromJobs));
        long completedRecords = Math.max(completedFromQueue, Math.max(completedFromJobs, completedFromItems));
        long pendingRecords = Math.max(0, totalRecords - completedRecords);
        int progressPercent = totalRecords == 0 ? 0 : (int) Math.round((completedRecords * 100.0) / totalRecords);
        boolean completed = completedRecords >= totalRecords && totalRecords > 0;

        return BatchProgressResponse.builder()
                .batchId(batchId)
                .totalRecords(totalRecords)
                .completedRecords(completedRecords)
                .pendingRecords(pendingRecords)
                .progressPercent(progressPercent)
                .completed(completed)
                .build();
    }

    private void ensureEmbossingJobsAreSynced() {
        embossingDataInitializer.syncEmbossingJobsFromProductionItems();
        embossingDataInitializer.syncEmbossingJobsFromQueue();
    }

    private String resolveActiveBatchId() {
        String configuredBatch = simulationProperties.getActiveBatch();
        if (!productionBatchItemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(configuredBatch).isEmpty()) {
            return configuredBatch;
        }

        List<ProductionBatchItem> fallbackItems = productionBatchItemRepository.findAll();
        if (!fallbackItems.isEmpty()) {
            return resolveBatchIdFromItem(fallbackItems.get(0));
        }

        return configuredBatch;
    }

    private String resolveBatchIdFromItem(ProductionBatchItem item) {
        return item.getProductionBatch() != null ? item.getProductionBatch().getBatchId() : simulationProperties.getActiveBatch();
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
