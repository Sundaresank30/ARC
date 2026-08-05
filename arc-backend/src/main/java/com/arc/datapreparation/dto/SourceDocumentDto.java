package com.arc.datapreparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceDocumentDto {
    private Long id;
    private String batchId;
    private String clientName;
    private String plant;
    private String product;
    private String vacuumSetpoint;
    private String maximumVacuum;
    private String minimumVacuum;
    private String warningThreshold;
    private String alarmThreshold;
    private String vacuumHoldTime;
    private String motorCurrent;
    private String motorTemperature;
    private String operatingPressure;
    private String cycleTime;
    private LocalDateTime uploadedAt;
}
