package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Syncs embossing jobs strictly with machine queue records in the database.
 */
@Component
@Order(2)
@Slf4j
public class EmbossingDataInitializer implements CommandLineRunner {

    private final EmbossingJobRepository embossingJobRepository;
    private final ProductionBatchItemRepository productionBatchItemRepository;
    private final EmbossingQueueRepository embossingQueueRepository;
    private final EmbossingSimulationProperties simulationProperties;

    public EmbossingDataInitializer(
            EmbossingJobRepository embossingJobRepository,
            ProductionBatchItemRepository productionBatchItemRepository,
            EmbossingQueueRepository embossingQueueRepository,
            EmbossingSimulationProperties simulationProperties) {
        this.embossingJobRepository = embossingJobRepository;
        this.productionBatchItemRepository = productionBatchItemRepository;
        this.embossingQueueRepository = embossingQueueRepository;
        this.simulationProperties = simulationProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        syncEmbossingJobsFromQueue();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncEmbossingJobsFromQueue() {
        List<EmbossingQueue> queueItems = embossingQueueRepository.findAll();
        if (queueItems.isEmpty()) {
            return;
        }

        for (EmbossingQueue qItem : queueItems) {
            List<EmbossingJob> existingJobs = embossingJobRepository
                    .findBySerialNumberAndPartNumber(qItem.getSerialNumber(), qItem.getPartNumber());
            EmbossingJob job = existingJobs.isEmpty() ? null : existingJobs.get(0);

            if (job == null) {
                List<ProductionBatchItem> items = productionBatchItemRepository
                        .findBySerialNumberAndPartNumber(qItem.getSerialNumber(), qItem.getPartNumber());
                ProductionBatchItem item = items.isEmpty() ? null : items.get(0);
                String batchId = (item != null && item.getProductionBatch() != null)
                        ? item.getProductionBatch().getBatchId()
                        : simulationProperties.getActiveBatch();

                EmbossingStatus status = qItem.getStatus() == EmbossingQueueStatus.COMPLETED
                        ? EmbossingStatus.COMPLETED
                        : qItem.getStatus() == EmbossingQueueStatus.IN_PROGRESS
                        ? EmbossingStatus.PRINTING
                        : EmbossingStatus.PENDING;

                EmbossingJob newJob = EmbossingJob.builder()
                        .batchId(batchId)
                        .partNumber(qItem.getPartNumber())
                        .serialNumber(qItem.getSerialNumber())
                        .embossingStatus(status)
                        .createdTime(LocalDateTime.now())
                        .embossingStartTime(status != EmbossingStatus.PENDING ? LocalDateTime.now() : null)
                        .embossingCompletedTime(status == EmbossingStatus.COMPLETED ? (qItem.getPrintedAt() != null ? qItem.getPrintedAt() : LocalDateTime.now()) : null)
                        .machineStatus(status == EmbossingStatus.COMPLETED ? MachineStatus.IDLE : status == EmbossingStatus.PRINTING ? MachineStatus.PRINTING : MachineStatus.WAITING)
                        .remarks("Synced from embossing queue")
                        .build();

                embossingJobRepository.save(newJob);
            } else if (qItem.getStatus() == EmbossingQueueStatus.COMPLETED && job.getEmbossingStatus() != EmbossingStatus.COMPLETED) {
                if (job.getEmbossingStartTime() == null) {
                    job.setEmbossingStartTime(LocalDateTime.now());
                }
                job.setEmbossingCompletedTime(qItem.getPrintedAt() != null ? qItem.getPrintedAt() : LocalDateTime.now());
                job.setEmbossingStatus(EmbossingStatus.COMPLETED);
                job.setMachineStatus(MachineStatus.IDLE);
                embossingJobRepository.save(job);
            }
        }
    }
}
