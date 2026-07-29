package com.arc.dashboard.service;

import com.arc.dashboard.entity.CarryForwardEmbossing;
import com.arc.dashboard.entity.LeakageFailure;
import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.dashboard.repository.LeakageFailureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DashboardDataInitializer implements CommandLineRunner {

    private final CarryForwardEmbossingRepository carryForwardRepository;
    private final LeakageFailureRepository leakageFailureRepository;

    public DashboardDataInitializer(
            CarryForwardEmbossingRepository carryForwardRepository,
            LeakageFailureRepository leakageFailureRepository) {
        this.carryForwardRepository = carryForwardRepository;
        this.leakageFailureRepository = leakageFailureRepository;
    }

    @Override
    public void run(String... args) {
        if (carryForwardRepository.count() == 0) {
            log.info("Initializing Carry Forward Embossing sample data...");
            carryForwardRepository.saveAll(List.of(
                    CarryForwardEmbossing.builder()
                            .partNo("Pn00111c")
                            .serialNo("P0011156")
                            .status("Pending")
                            .remainingSince("17:57, 20 Jul")
                            .nextShift("21 Jul")
                            .action("Queued")
                            .build(),
                    CarryForwardEmbossing.builder()
                            .partNo("Pn00112c")
                            .serialNo("P0011157")
                            .status("Pending")
                            .remainingSince("17:58, 20 Jul")
                            .nextShift("21 Jul")
                            .action("Queued")
                            .build(),
                    CarryForwardEmbossing.builder()
                            .partNo("Pn00113c")
                            .serialNo("P0011158")
                            .status("Pending")
                            .remainingSince("18:00, 20 Jul")
                            .nextShift("21 Jul")
                            .action("Queued")
                            .build()
            ));
        }

        if (leakageFailureRepository.count() == 0) {
            log.info("Initializing Leakage Failure sample data...");
            leakageFailureRepository.saveAll(List.of(
                    LeakageFailure.builder()
                            .partNo("Pn00111c")
                            .serialNo("P0011156")
                            .status("Failed")
                            .testValue(0.42)
                            .direction("down")
                            .timestamp("17:57, 20 Jul")
                            .attempt("2/2")
                            .action("Scrap")
                            .build(),
                    LeakageFailure.builder()
                            .partNo("Pn00112c")
                            .serialNo("P0011157")
                            .status("Failed")
                            .testValue(1.08)
                            .direction("up")
                            .timestamp("17:58, 20 Jul")
                            .attempt("1/2")
                            .action("Pending")
                            .build(),
                    LeakageFailure.builder()
                            .partNo("Pn00113c")
                            .serialNo("P0011158")
                            .status("Failed")
                            .testValue(0.48)
                            .direction("down")
                            .timestamp("18:00, 20 Jul")
                            .attempt("1/2")
                            .action("Pending")
                            .build()
            ));
        }
    }
}
