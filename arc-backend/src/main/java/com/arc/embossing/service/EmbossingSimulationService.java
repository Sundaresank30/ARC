package com.arc.embossing.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.dto.SimulationStartResponse;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.exception.SimulationAlreadyRunningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a physical embossing machine that processes one job at a time.
 * Each job transitions through PENDING → IN_MACHINE → PRINTING → COMPLETED.
 */
@Service
@Slf4j
public class EmbossingSimulationService {

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingJobStateService embossingJobStateService;
    private final EmbossingSimulationProperties simulationProperties;

    private final AtomicBoolean simulationRunning = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "embossing-simulation");
        thread.setDaemon(true);
        return thread;
    });

    public EmbossingSimulationService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingJobStateService embossingJobStateService,
            EmbossingSimulationProperties simulationProperties) {
        this.embossingJobRepository = embossingJobRepository;
        this.embossingJobStateService = embossingJobStateService;
        this.simulationProperties = simulationProperties;
    }

    public SimulationStartResponse startSimulation() {
        if (!simulationRunning.compareAndSet(false, true)) {
            throw new SimulationAlreadyRunningException("Embossing simulation is already running");
        }

        executorService.submit(this::runSimulationLoop);

        log.info("Embossing simulation started");
        return SimulationStartResponse.builder()
                .message("Embossing simulation started successfully")
                .simulationRunning(true)
                .build();
    }

    public boolean isSimulationRunning() {
        return simulationRunning.get();
    }

    private void runSimulationLoop() {
        try {
            while (true) {
                Optional<EmbossingJob> nextJob = findNextPendingJob();
                if (nextJob.isEmpty()) {
                    log.debug("No pending embossing jobs yet; waiting for new work.");
                    sleep(1000L);
                    continue;
                }

                processJob(nextJob.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Embossing simulation interrupted");
        } catch (Exception e) {
            log.error("Embossing simulation failed", e);
        } finally {
            simulationRunning.set(false);
        }
    }

    private Optional<EmbossingJob> findNextPendingJob() {
        return embossingJobRepository.findFirstByEmbossingStatusOrderByIdDesc(EmbossingStatus.PENDING);
    }

    private void processJob(EmbossingJob job) throws InterruptedException {
        log.info("Moving job {} ({}) to machine", job.getPartNumber(), job.getSerialNumber());
        embossingJobStateService.transitionToInMachine(job.getId());
        sleep(simulationProperties.getInMachineDelayMs());

        log.info("Printing job {} ({})", job.getPartNumber(), job.getSerialNumber());
        embossingJobStateService.transitionToPrinting(job.getId());
        sleep(simulationProperties.getPrintingDelayMs());

        log.info("Completed job {} ({})", job.getPartNumber(), job.getSerialNumber());
        embossingJobStateService.transitionToCompleted(job.getId());
    }

    private void sleep(long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
    }
}
