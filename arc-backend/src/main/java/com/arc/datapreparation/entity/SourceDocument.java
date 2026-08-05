package com.arc.datapreparation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "source_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "plant")
    private String plant;

    @Column(name = "product")
    private String product;

    @Column(name = "vacuum_setpoint")
    private String vacuumSetpoint;

    @Column(name = "maximum_vacuum")
    private String maximumVacuum;

    @Column(name = "minimum_vacuum")
    private String minimumVacuum;

    @Column(name = "warning_threshold")
    private String warningThreshold;

    @Column(name = "alarm_threshold")
    private String alarmThreshold;

    @Column(name = "vacuum_hold_time")
    private String vacuumHoldTime;

    @Column(name = "motor_current")
    private String motorCurrent;

    @Column(name = "motor_temperature")
    private String motorTemperature;

    @Column(name = "operating_pressure")
    private String operatingPressure;

    @Column(name = "cycle_time")
    private String cycleTime;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
