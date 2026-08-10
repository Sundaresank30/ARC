package com.arc.datapreparation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductionBatchRequest {

    private static final String ALLOWED_CHARS_PATTERN = "^[a-zA-Z0-9_\\-\\s]+$";

    @NotBlank(message = "Batch ID is required")
    @Size(max = 100, message = "Batch ID cannot exceed 100 characters")
    @Pattern(regexp = ALLOWED_CHARS_PATTERN, message = "Batch ID contains invalid characters or path traversal sequences")
    private String batchId;

    @NotBlank(message = "Part number series is required")
    @Size(max = 100, message = "Part number series cannot exceed 100 characters")
    @Pattern(regexp = ALLOWED_CHARS_PATTERN, message = "Part number series contains invalid characters or path traversal sequences")
    private String partNoSeries;

    @NotNull(message = "Part number count is required")
    @Min(value = 1, message = "Part number count must be at least 1")
    @Max(value = 999, message = "Part number count cannot exceed 999")
    private Integer partNoCount;

    @NotBlank(message = "Serial number series is required")
    @Size(max = 100, message = "Serial number series cannot exceed 100 characters")
    @Pattern(regexp = ALLOWED_CHARS_PATTERN, message = "Serial number series contains invalid characters or path traversal sequences")
    private String serialNoSeries;

    @NotNull(message = "Serial number count is required")
    @Min(value = 1, message = "Serial number count must be at least 1")
    @Max(value = 999, message = "Serial number count cannot exceed 999")
    private Integer serialNoCount;
}
