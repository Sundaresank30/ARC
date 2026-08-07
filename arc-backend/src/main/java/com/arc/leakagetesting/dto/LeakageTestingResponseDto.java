package com.arc.leakagetesting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeakageTestingResponseDto {
    private String activeBatch;
    private long failedCount;
    private long passedCount;
    private int batchProgressPercent;
    private long completedCount;
    private long totalParts;
    private String dateDisplay;
    private String batchStatus;
    private List<LeakageTestItemDto> failures;
    private List<LeakageTestItemDto> passed;
}
