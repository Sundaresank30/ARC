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
public class StartEmbossingRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Machine Code is required")
    private String machineCode;

    private String productionLineDetails;
}
