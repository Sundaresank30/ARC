package com.arc.embossing.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with embossing jobs in the `embossing_jobs` table.
 * All items coming from completed embossing are marked COMPLETED initially,
 * so the Leakage Testing table starts clean with 0 failures unless an item fails.
 */
@Component
@Order(2)
@Slf4j
public class EmbossingDataInitializer implements CommandLineRunner {

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingSimulationProperties simulationProperties;

    public EmbossingDataInitializer(
            EmbossingJobRepository embossingJobRepository,
            EmbossingSimulationProperties simulationProperties) {
        this.embossingJobRepository = embossingJobRepository;
        this.simulationProperties = simulationProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (embossingJobRepository.count() > 0) {
            log.info("Embossing jobs already exist. Skipping dummy data initialization.");
            return;
        }

        List<EmbossingJob> seededJobs = createSeededJobs();
        embossingJobRepository.saveAll(seededJobs);

        log.info("Inserted {} completed embossing jobs for batch {}", seededJobs.size(),
                simulationProperties.getActiveBatch());
    }

    private List<EmbossingJob> createSeededJobs() {
        List<EmbossingJob> jobs = new ArrayList<>();
        String batchId = simulationProperties.getActiveBatch() != null ? simulationProperties.getActiveBatch() : "Batch_1";
        LocalDateTime now = LocalDateTime.now();

        // Seed 100 COMPLETED embossing jobs for Batch_1
        for (int i = 1; i <= 100; i++) {
            jobs.add(EmbossingJob.builder()
                    .batchId(batchId)
                    .partNumber(String.format("Pn%05dc", i))
                    .serialNumber(String.format("P%07d", 10000 + i))
                    .embossingStatus(EmbossingStatus.COMPLETED)
                    .createdTime(now.minusHours(2).plusMinutes(i))
                    .embossingCompletedTime(now.minusHours(1).plusMinutes(i))
                    .machineStatus(MachineStatus.IDLE)
                    .remarks("Embossing completed successfully")
                    .build());
        }

        return jobs;
    }
}
