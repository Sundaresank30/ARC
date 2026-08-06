package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.dto.BatchProgressResponse;
import com.arc.embossing.dto.EmbossingJobResponse;
import com.arc.embossing.dto.EmbossingProgressDTO;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.mapper.EmbossingJobMapper;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.machine.entity.EmbossingQueue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmbossingJobService {
    private final EmbossingJobRepository jobRepository;
    private final ProductionBatchItemRepository productionItemRepository;
    private final EmbossingJobMapper jobMapper;
    private final EmbossingService embossingService;
    private final EmbossingProgressPublisher progressPublisher;

    public EmbossingJobService(EmbossingJobRepository jobRepository,
                               ProductionBatchItemRepository productionItemRepository,
                               EmbossingJobMapper jobMapper,
                               EmbossingService embossingService,
                               EmbossingProgressPublisher progressPublisher) {
        this.jobRepository = jobRepository;
        this.productionItemRepository = productionItemRepository;
        this.jobMapper = jobMapper;
        this.embossingService = embossingService;
        this.progressPublisher = progressPublisher;
    }

    @Transactional
    public void recordEmbossingStarted(EmbossingQueue queueItem) {
        List<EmbossingJob> existingJobs = jobRepository.findBySerialNumberAndPartNumberForUpdate(
                queueItem.getSerialNumber(), queueItem.getPartNumber());

        EmbossingJob job;
        if (!existingJobs.isEmpty()) {
            job = existingJobs.get(0);
        } else {
            List<ProductionBatchItem> items = productionItemRepository
                    .findBySerialNumberAndPartNumber(queueItem.getSerialNumber(), queueItem.getPartNumber());
            ProductionBatchItem item = items.isEmpty() ? null : items.get(0);
            String batchId = (item != null && item.getProductionBatch() != null)
                    ? item.getProductionBatch().getBatchId()
                    : "Batch_1";

            job = EmbossingJob.builder()
                    .batchId(batchId)
                    .partNumber(queueItem.getPartNumber())
                    .serialNumber(queueItem.getSerialNumber())
                    .embossingStatus(EmbossingStatus.PENDING)
                    .createdTime(LocalDateTime.now())
                    .machineStatus(MachineStatus.WAITING)
                    .remarks("Created on machine in-progress")
                    .build();
        }

        if (job.getEmbossingStatus() == EmbossingStatus.COMPLETED) {
            return;
        }

        if (job.getEmbossingStartTime() == null) {
            job.setEmbossingStartTime(LocalDateTime.now());
        }
        job.setEmbossingStatus(EmbossingStatus.PRINTING);
        job.setMachineStatus(MachineStatus.PRINTING);
        EmbossingJob savedJob = jobRepository.save(job);

        List<ProductionBatchItem> batchItems = productionItemRepository
                .findByProductionBatchBatchIdOrderByItemIndexAsc(savedJob.getBatchId());
        BatchProgressResponse batchProgress = embossingService.buildBatchProgress(savedJob.getBatchId(), batchItems);
        long completedFromJobs = jobRepository.countByBatchIdAndEmbossingStatus(savedJob.getBatchId(), EmbossingStatus.COMPLETED);
        long completedFromItems = batchItems != null ? batchItems.stream()
                .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                .count() : 0;
        long totalFromItems = batchItems != null ? batchItems.size() : 0;
        long totalFromJobs = jobRepository.countByBatchId(savedJob.getBatchId());
        long total = Math.max(totalFromItems, totalFromJobs);
        long completed = Math.max(completedFromJobs, completedFromItems);

        progressPublisher.publishAfterCommit(EmbossingProgressDTO.builder()
                .jobId(savedJob.getId())
                .batchId(savedJob.getBatchId())
                .jobStatus(savedJob.getEmbossingStatus())
                .totalCount(total)
                .completedCount(completed)
                .pendingCount(Math.max(0, total - completed))
                .progressPercent(batchProgress.getProgressPercent())
                .completed(completed >= total && total > 0)
                .job(jobMapper.toResponse(savedJob))
                .batchProgress(batchProgress)
                .build());
    }

    /**
     * Must be called only after the physical machine reports a successful card.
     * It joins the caller transaction, so queue, card job and source item commit together.
     */
    @Transactional
    public void recordSuccessfulEmbossing(EmbossingQueue queueItem) {
        List<EmbossingJob> existingJobs = jobRepository.findBySerialNumberAndPartNumberForUpdate(
                queueItem.getSerialNumber(), queueItem.getPartNumber());

        EmbossingJob job;
        if (!existingJobs.isEmpty()) {
            job = existingJobs.get(0);
        } else {
            List<ProductionBatchItem> items = productionItemRepository
                    .findBySerialNumberAndPartNumber(queueItem.getSerialNumber(), queueItem.getPartNumber());
            ProductionBatchItem item = items.isEmpty() ? null : items.get(0);
            String batchId = (item != null && item.getProductionBatch() != null)
                    ? item.getProductionBatch().getBatchId()
                    : "Batch_1";

            job = EmbossingJob.builder()
                    .batchId(batchId)
                    .partNumber(queueItem.getPartNumber())
                    .serialNumber(queueItem.getSerialNumber())
                    .embossingStatus(EmbossingStatus.PENDING)
                    .createdTime(LocalDateTime.now())
                    .machineStatus(MachineStatus.WAITING)
                    .remarks("Created on machine printing")
                    .build();
        }

        if (job.getEmbossingStatus() == EmbossingStatus.COMPLETED) {
            return;
        }

        if (job.getEmbossingStartTime() == null) {
            job.setEmbossingStartTime(LocalDateTime.now());
        }
        job.setEmbossingCompletedTime(LocalDateTime.now());
        job.setEmbossingStatus(EmbossingStatus.COMPLETED);
        job.setMachineStatus(MachineStatus.IDLE);
        EmbossingJob savedJob = jobRepository.save(job);

        List<ProductionBatchItem> preparedItems = productionItemRepository
                .findBySerialNumberAndPartNumber(queueItem.getSerialNumber(), queueItem.getPartNumber());

        for (ProductionBatchItem preparedItem : preparedItems) {
            preparedItem.setStatus("COMPLETED");
            productionItemRepository.save(preparedItem);
        }

        List<ProductionBatchItem> batchItems = productionItemRepository
                .findByProductionBatchBatchIdOrderByItemIndexAsc(savedJob.getBatchId());
        BatchProgressResponse batchProgress = embossingService.buildBatchProgress(savedJob.getBatchId(), batchItems);
        long completedFromJobs = jobRepository.countByBatchIdAndEmbossingStatus(savedJob.getBatchId(), EmbossingStatus.COMPLETED);
        long completedFromItems = batchItems != null ? batchItems.stream()
                .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
                .count() : 0;
        long totalFromItems = batchItems != null ? batchItems.size() : 0;
        long totalFromJobs = jobRepository.countByBatchId(savedJob.getBatchId());
        long total = Math.max(totalFromItems, totalFromJobs);
        long completed = Math.max(completedFromJobs, completedFromItems);

        progressPublisher.publishAfterCommit(EmbossingProgressDTO.builder()
                .jobId(savedJob.getId())
                .batchId(savedJob.getBatchId())
                .jobStatus(savedJob.getEmbossingStatus())
                .totalCount(total)
                .completedCount(completed)
                .pendingCount(Math.max(0, total - completed))
                .progressPercent(batchProgress.getProgressPercent())
                .completed(completed >= total && total > 0)
                .job(jobMapper.toResponse(savedJob))
                .batchProgress(batchProgress)
                .build());
    }
}
