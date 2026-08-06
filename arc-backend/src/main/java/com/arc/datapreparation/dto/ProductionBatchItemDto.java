package com.arc.datapreparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBatchItemDto {
    private Long id;
    private String batchId;
    private Integer itemIndex;
    private String partNumber;
    private String serialNumber;
    private String status;
}
