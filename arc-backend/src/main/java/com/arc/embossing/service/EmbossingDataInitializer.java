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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Syncs embossing jobs with production batch items and machine queue records in the database.
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
        syncEmbossingJobsFromProductionItems();
        syncEmbossingJobsFromQueue();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncEmbossingJobsFromProductionItems() {
        List<ProductionBatchItem> batchItems = productionBatchItemRepository.findAll();
        if (batchItems.isEmpty()) {
            log.info("No production batch items found for active batch {}. The module will wait for real production data.",
                    simulationProperties.getActiveBatch());
            return;
        }

        Set<String> seenPartNumbers = new HashSet<>();
        Set<String> seenSerialNumbers = new HashSet<>();
        embossingJobRepository.findAll().forEach(job -> {
            seenPartNumbers.add(normalizeIdentifier(job.getPartNumber()));
            seenSerialNumbers.add(normalizeIdentifier(job.getSerialNumber()));
        });
        List<EmbossingJob> initializationJobs = new ArrayList<>();

        for (ProductionBatchItem item : batchItems) {
            String partNumber = item.getPartNumber();
            String serialNumber = item.getSerialNumber();
            if (!seenPartNumbers.add(normalizeIdentifier(partNumber))
                    || !seenSerialNumbers.add(normalizeIdentifier(serialNumber))) {
                log.warn("Skipping duplicate production item for part number {} and serial number {}", partNumber, serialNumber);
                continue;
            }

            boolean alreadyExists = embossingJobRepository.existsByPartNumber(partNumber)
                    || embossingJobRepository.existsBySerialNumber(serialNumber);
            if (alreadyExists) {
                continue;
            }

            initializationJobs.add(toEmbossingJob(item));
        }

        if (!initializationJobs.isEmpty()) {
            embossingJobRepository.saveAll(initializationJobs);
            log.info("Synced {} embossing jobs from production batch items for active batch {}",
                    initializationJobs.size(), simulationProperties.getActiveBatch());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncEmbossingJobsFromQueue() {
        List<EmbossingQueue> queueItems = embossingQueueRepository.findAll();
        if (queueItems.isEmpty()) {
            return;
        }

        for (EmbossingQueue qItem : queueItems) {
            EmbossingJob job = embossingJobRepository
                    .findBySerialNumberAndPartNumber(qItem.getSerialNumber(), qItem.getPartNumber())
                    .orElse(null);

            if (job == null) {
                ProductionBatchItem item = productionBatchItemRepository
                        .findBySerialNumberAndPartNumber(qItem.getSerialNumber(), qItem.getPartNumber())
                        .orElse(null);
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

    private EmbossingJob toEmbossingJob(ProductionBatchItem item) {
        EmbossingStatus status = "COMPLETED".equalsIgnoreCase(item.getStatus())
                ? EmbossingStatus.COMPLETED
                : EmbossingStatus.PENDING;

        return EmbossingJob.builder()
                .batchId(item.getProductionBatch() != null ? item.getProductionBatch().getBatchId() : simulationProperties.getActiveBatch())
                .partNumber(item.getPartNumber())
                .serialNumber(item.getSerialNumber())
                .embossingStatus(status)
                .createdTime(LocalDateTime.now())
                .machineStatus(status == EmbossingStatus.COMPLETED ? MachineStatus.IDLE : MachineStatus.WAITING)
                .remarks("Synced from production batch item")
                .build();
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
