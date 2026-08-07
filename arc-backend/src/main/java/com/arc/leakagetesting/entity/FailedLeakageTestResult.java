package com.arc.leakagetesting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_leakage_test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedLeakageTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "pressure_value")
    private Double pressureValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "warning_threshold")
    private Double warningThreshold;

    @Column(name = "alarm_threshold")
    private Double alarmThreshold;

    @Column(name = "status")
    private String status; // FAILED

    @Column(name = "cycle_time_seconds")
    private Double cycleTimeSeconds;

    @Column(name = "attempt")
    private String attempt;

    @Column(name = "action")
    private String action; // Pending, Scrap

    @Column(name = "direction")
    private String direction;

    @Column(name = "tested_at")
    private LocalDateTime testedAt;

    @PrePersist
    protected void onCreate() {
        if (testedAt == null) {
            testedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "FAILED";
        }
        if (action == null) {
            action = "Pending";
        }
    }
}
