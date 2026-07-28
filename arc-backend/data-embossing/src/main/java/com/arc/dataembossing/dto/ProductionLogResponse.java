package com.arc.dataembossing.dto;

import com.arc.dataembossing.entity.ProductionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogResponse {

    private Long id;
    private String batchId;
    private String productionLineDetails;
    private String machineCode;
    private String machineResponse;
    private String status;
    private String logMessage;
    private LocalDateTime timestamp;

    public static ProductionLogResponse fromEntity(ProductionLog logEntity) {
        return ProductionLogResponse.builder()
                .id(logEntity.getId())
                .batchId(logEntity.getBatch() != null ? logEntity.getBatch().getBatchId() : null)
                .productionLineDetails(logEntity.getProductionLineDetails())
                .machineCode(logEntity.getMachineCode())
                .machineResponse(logEntity.getMachineResponse())
                .status(logEntity.getStatus())
                .logMessage(logEntity.getLogMessage())
                .timestamp(logEntity.getTimestamp())
                .build();
    }
}
