package com.arc.datapreparation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Skipping auto-creation of dummy production batches.
 * Only real production batches created by users are stored and displayed.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class DataPreparationDataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("DataPreparationDataInitializer: Dummy batch seeding disabled. Only real user batches will be stored.");
    }
}
