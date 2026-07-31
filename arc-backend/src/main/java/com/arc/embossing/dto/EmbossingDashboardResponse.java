package com.arc.embossing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbossingDashboardResponse {

    private String activeBatch;
    private long pendingCount;
    private List<EmbossingJobResponse> jobs;
}
