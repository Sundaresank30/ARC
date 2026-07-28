package com.arc.datapreparation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotEmpty(message = "At least one Part Number Series is required")
    private List<String> partNumberSeries;

    @NotEmpty(message = "At least one Serial Number Series is required")
    private List<String> serialNumberSeries;

    @NotNull(message = "Total count is required")
    @Positive(message = "Total count must be greater than zero")
    private Integer totalCount;
}
