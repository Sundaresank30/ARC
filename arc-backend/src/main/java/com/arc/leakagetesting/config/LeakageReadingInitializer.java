package com.arc.leakagetesting.config;

import com.arc.leakagetesting.entity.LeakageMachineReading;
import com.arc.leakagetesting.repository.LeakageMachineReadingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds sample database readings for Leakage Machine testing strictly when table is empty.
 */
@Component
@Order(3)
@Slf4j
public class LeakageReadingInitializer implements CommandLineRunner {

    private final LeakageMachineReadingRepository readingRepository;

    public LeakageReadingInitializer(LeakageMachineReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (readingRepository.count() == 0) {
            log.info("Seeding initial sample measured values into leakage_machine_readings database table...");
            double[] sampleValues = new double[] { 76.0, 78.0, 82.0, 77.0, 84.0 };
            for (double val : sampleValues) {
                LeakageMachineReading reading = LeakageMachineReading.builder()
                        .measuredValue(val)
                        .unit("kPa")
                        .readingStatus("Available")
                        .createdAt(LocalDateTime.now())
                        .build();
                readingRepository.save(reading);
            }
            log.info("Successfully seeded {} sample measured readings into database table leakage_machine_readings.", sampleValues.length);
        } else {
            log.info("leakage_machine_readings database table contains existing data. Skipping seed.");
        }
    }
}
