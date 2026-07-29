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
 * Seeds the database with dummy embossing jobs on application startup.
 * Skips initialization if jobs already exist.
 */
@Component
@Order(2)
@Slf4j
public class EmbossingDataInitializer implements CommandLineRunner {

    private static final int PART_NUMBER_BASE = 24001;
    private static final int SERIAL_NUMBER_BASE = 840001;

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

        List<EmbossingJob> dummyJobs = createDummyJobs();
        embossingJobRepository.saveAll(dummyJobs);

        log.info("Inserted {} dummy embossing jobs for batch {}", dummyJobs.size(),
                simulationProperties.getActiveBatch());
    }

    private List<EmbossingJob> createDummyJobs() {
        List<EmbossingJob> jobs = new ArrayList<>();
        String batchId = simulationProperties.getActiveBatch();
        int jobCount = simulationProperties.getDummyJobCount();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < jobCount; i++) {
            jobs.add(EmbossingJob.builder()
                    .batchId(batchId)
                    .partNumber("PN" + (PART_NUMBER_BASE + i))
                    .serialNumber("SR" + (SERIAL_NUMBER_BASE + i))
                    .embossingStatus(EmbossingStatus.PENDING)
                    .createdTime(now.minusMinutes(jobCount - i))
                    .machineStatus(MachineStatus.WAITING)
                    .remarks("Dummy simulated job from previous production stage")
                    .build());
        }

        return jobs;
    }
}
