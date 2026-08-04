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
        log.info("DashboardDataInitializer: Skipping sample dummy data initialization.");
    }
}
