package com.arc.dataembossing.dto;

import com.arc.dataembossing.entity.Batch;
import com.arc.dataembossing.entity.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbossingResponse {

    private Long id;
    private String batchId;
    private BatchStatus status;
    private Integer totalCount;
    private Integer completedCount;
    private Integer failedCount;
    private Double progressPercentage;
    private LocalDateTime lastUpdated;

    public static EmbossingResponse fromBatch(Batch batch) {
        double progress = 0.0;
        if (batch.getTotalCount() != null && batch.getTotalCount() > 0) {
            int processed = (batch.getCompletedCount() != null ? batch.getCompletedCount() : 0) +
                            (batch.getFailedCount() != null ? batch.getFailedCount() : 0);
            progress = Math.min(100.0, ((double) processed / batch.getTotalCount()) * 100.0);
        }

        return EmbossingResponse.builder()
                .id(batch.getId())
                .batchId(batch.getBatchId())
                .status(batch.getStatus())
                .totalCount(batch.getTotalCount())
                .completedCount(batch.getCompletedCount())
                .failedCount(batch.getFailedCount())
                .progressPercentage(Math.round(progress * 100.0) / 100.0)
                .lastUpdated(batch.getUpdatedAt())
                .build();
    }
}
