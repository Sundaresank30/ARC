package com.arc.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeakageFailureDTO {
    private String id;
    private String partNo;
    private String serialNo;
    private String status;
    private Double testValue;
    private String direction;
    private String timestamp;
    private String attempt;
    private String action;
}
