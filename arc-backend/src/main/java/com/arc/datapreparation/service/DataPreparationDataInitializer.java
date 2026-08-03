package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds initial Data Preparation production batch on application startup if empty.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class DataPreparationDataInitializer implements CommandLineRunner {

    private final ProductionBatchRepository batchRepository;
    private final DataPreparationService dataPreparationService;

    @Override
    public void run(String... args) {
        if (batchRepository.count() > 0) {
            log.info("Production batches already exist in Data Preparation. Skipping seed.");
            return;
        }

        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("BATCH-001")
                .partNoSeries("PN-A89")
                .partNoCount(5)
                .serialNoSeries("SN-1001")
                .serialNoCount(5)
                .build();

        dataPreparationService.createProductionBatch(request);
        log.info("Seeded initial Data Preparation batch BATCH-001 with 5 items.");
    }
}
