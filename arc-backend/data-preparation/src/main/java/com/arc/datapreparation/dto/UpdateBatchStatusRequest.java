package com.arc.datapreparation.dto;

import com.arc.datapreparation.entity.BatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBatchStatusRequest {

    @NotNull(message = "Status is required")
    private BatchStatus status;
}
