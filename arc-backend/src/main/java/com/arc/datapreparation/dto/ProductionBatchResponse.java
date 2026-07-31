package com.arc.datapreparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBatchResponse {
    private Long id;
    private String batchId;
    private String partNoSeries;
    private Integer partNoCount;
    private String serialNoSeries;
    private Integer serialNoCount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private List<ProductionBatchItemDto> items;
}
