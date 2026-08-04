package com.arc.embossing.dto;

import com.arc.embossing.enums.EmbossingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbossingProgressDTO {
    private Long jobId;
    private String batchId;
    private EmbossingStatus jobStatus;
    private long totalCount;
    private long completedCount;
    private long pendingCount;
    private int progressPercent;
    private boolean completed;
    private EmbossingJobResponse job;
    private BatchProgressResponse batchProgress;
}
