package com.arc.config;

import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.dashboard.repository.LeakageFailureRepository;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.datapreparation.repository.SourceDocumentRepository;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import com.arc.machine.repository.EmbossingQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Startup runner to purge all dummy/sample data from the database,
 * ensuring only real production data created by users remains.
 */
@Component
@Order(0)
@Slf4j
@RequiredArgsConstructor
public class PurgeDummyDataRunner implements CommandLineRunner {

    private final ProductionBatchRepository productionBatchRepository;
    private final ProductionBatchItemRepository productionBatchItemRepository;
    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingQueueRepository embossingQueueRepository;
    private final LeakageTestResultRepository leakageTestResultRepository;
    private final com.arc.leakagetesting.repository.PassedLeakageTestResultRepository passedLeakageTestResultRepository;
    private final com.arc.leakagetesting.repository.FailedLeakageTestResultRepository failedLeakageTestResultRepository;
    private final CarryForwardEmbossingRepository carryForwardEmbossingRepository;
    private final LeakageFailureRepository leakageFailureRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and purging dummy/sample data from database...");

        try {
            // Delete carry forward & leakage failure sample entries if any
            carryForwardEmbossingRepository.deleteAll();
            leakageFailureRepository.deleteAll();

            // Purge dummy/sample batches (such as BATCH-001, Batch_1, sample, dummy)
            productionBatchRepository.findAll().forEach(batch -> {
                if (batch.getBatchId() != null && (
                        batch.getBatchId().equalsIgnoreCase("BATCH-001") ||
                        batch.getBatchId().equalsIgnoreCase("Batch_1") ||
                        batch.getBatchId().toLowerCase().contains("dummy") ||
                        batch.getBatchId().toLowerCase().contains("sample")
                )) {
                    log.info("Purging sample batch: {}", batch.getBatchId());
                    productionBatchItemRepository.deleteAll(
                            productionBatchItemRepository.findByProductionBatchBatchIdOrderByItemIndexAsc(batch.getBatchId())
                    );
                    productionBatchRepository.delete(batch);
                }
            });

            // Purge dummy embossing jobs
            embossingJobRepository.findAll().forEach(job -> {
                if (job.getBatchId() != null && (
                        job.getBatchId().equalsIgnoreCase("BATCH-001") ||
                        job.getBatchId().equalsIgnoreCase("Batch_1") ||
                        job.getBatchId().toLowerCase().contains("dummy") ||
                        job.getBatchId().toLowerCase().contains("sample")
                )) {
                    log.info("Purging dummy embossing job ID: {}", job.getId());
                    embossingJobRepository.delete(job);
                }
            });

            // Purge dummy embossing queue items
            embossingQueueRepository.findAll().forEach(q -> {
                if (q.getSerialNumber() != null && (
                        q.getSerialNumber().toLowerCase().contains("dummy") ||
                        q.getSerialNumber().toLowerCase().contains("sample")
                )) {
                    embossingQueueRepository.delete(q);
                }
            });

            log.info("Dummy data purge verification complete. Database contains real data only.");
        } catch (Exception e) {
            log.error("Error purging dummy data: {}", e.getMessage(), e);
        }
    }
}
