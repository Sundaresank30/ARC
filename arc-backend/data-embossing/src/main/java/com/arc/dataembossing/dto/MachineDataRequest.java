package com.arc.dataembossing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineDataRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Machine Code is required")
    private String machineCode;

    private String productionLineDetails;

    private Integer successIncrement; // number of newly embossed units

    private Integer failureIncrement; // number of failed units

    private String machineResponse;

    private String status; // SUCCESS, WARNING, ERROR

    private String logMessage;
}
