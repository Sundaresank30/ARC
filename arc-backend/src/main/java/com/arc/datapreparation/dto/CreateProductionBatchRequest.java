package com.arc.datapreparation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductionBatchRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Part number series is required")
    private String partNoSeries;

    @NotNull(message = "Part number count is required")
    @Min(value = 1, message = "Part number count must be at least 1")
    @Max(value = 999, message = "Part number count cannot exceed 999")
    private Integer partNoCount;

    @NotBlank(message = "Serial number series is required")
    private String serialNoSeries;

    @NotNull(message = "Serial number count is required")
    @Min(value = 1, message = "Serial number count must be at least 1")
    @Max(value = 999, message = "Serial number count cannot exceed 999")
    private Integer serialNoCount;
}
