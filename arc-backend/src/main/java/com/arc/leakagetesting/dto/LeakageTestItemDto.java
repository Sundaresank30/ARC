package com.arc.leakagetesting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeakageTestItemDto {
    private Long id;
    private String partNo;
    private String serialNo;
    private String status;       // "Failed", "Passed"
    private Double testValue;    // e.g. 0.42, 1.08, 0.48
    private String direction;    // "down", "up"
    private String timestamp;    // e.g. "17:57, 20 Jul"
    private String attempt;      // e.g. "2/2", "1/2"
    private String action;       // "Scrap", "Pending"
}
