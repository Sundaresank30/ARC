package com.arc.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarryForwardDTO {
    private String id;
    private String partNo;
    private String serialNo;
    private String status;
    private String remainingSince;
    private String nextShift;
    private String action;
}
