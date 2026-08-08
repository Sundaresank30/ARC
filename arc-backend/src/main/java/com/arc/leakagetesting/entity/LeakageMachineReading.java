package com.arc.leakagetesting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leakage_machine_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeakageMachineReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "measured_value", nullable = false)
    private Double measuredValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "reading_status", nullable = false)
    private String readingStatus; // Available or Used

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (unit == null || unit.isBlank()) {
            unit = "kPa";
        }
        if (readingStatus == null || readingStatus.isBlank()) {
            readingStatus = "Available";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
