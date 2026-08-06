package com.arc.leakagetesting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class LeakageMachineDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MachineState {
        private String machineStatus; // IDLE, TESTING, PAUSED, COMPLETED
        private String activeBatch;
        private String fileName;
        private Double warningThreshold;
        private Double alarmThreshold;
        private String unit;
        private Long totalEmbossed;
        private Long totalTested;
        private Long passedParts;
        private Long failedParts;
        private Double progressPercent;
        private LiveChamber activeChamber;
        private List<TrendPoint> trendData;
        private List<QueueItem> queue;
        private List<TestedRecord> history;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LiveChamber {
        private String batchId;
        private String partNumber;
        private String serialNumber;
        private Double currentPressure;
        private String unit;
        private Double warningThreshold;
        private Double alarmThreshold;
        private String status; // TESTING, PASSED, FAILED
        private Double cycleTimeSeconds;
        private String timestamp;
        private List<Double> pressureReadings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String serialNumber;
        private String partNumber;
        private Double pressureValue;
        private Boolean passed;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueItem {
        private Long id;
        private String batchId;
        private String partNumber;
        private String serialNumber;
        private String status; // READY, TESTING, COMPLETED, FAILED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestedRecord {
        private Long id;
        private String batchId;
        private String partNumber;
        private String serialNumber;
        private Double pressureValue;
        private String unit;
        private String status; // PASSED or FAILED
        private String timestamp;
    }
}
