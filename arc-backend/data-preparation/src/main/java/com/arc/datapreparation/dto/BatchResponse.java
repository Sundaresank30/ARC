package com.arc.datapreparation.dto;

import com.arc.datapreparation.entity.Batch;
import com.arc.datapreparation.entity.BatchStatus;
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
public class BatchResponse {

    private Long id;
    private String batchId;
    private BatchStatus status;
    private List<String> partNumberSeries;
    private List<String> serialNumberSeries;
    private Integer totalCount;
    private Integer completedCount;
    private Integer failedCount;
    private Double progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BatchResponse fromEntity(Batch batch) {
        double progress = 0.0;
        if (batch.getTotalCount() != null && batch.getTotalCount() > 0) {
            int processed = (batch.getCompletedCount() != null ? batch.getCompletedCount() : 0) +
                            (batch.getFailedCount() != null ? batch.getFailedCount() : 0);
            progress = Math.min(100.0, ((double) processed / batch.getTotalCount()) * 100.0);
        }

        return BatchResponse.builder()
                .id(batch.getId())
                .batchId(batch.getBatchId())
                .status(batch.getStatus())
                .partNumberSeries(batch.getPartNumberSeries())
                .serialNumberSeries(batch.getSerialNumberSeries())
                .totalCount(batch.getTotalCount())
                .completedCount(batch.getCompletedCount())
                .failedCount(batch.getFailedCount())
                .progressPercentage(Math.round(progress * 100.0) / 100.0)
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }
}
