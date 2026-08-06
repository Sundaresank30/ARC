package com.arc.embossing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchProgressResponse {

    private String batchId;
    private int progressPercent;
    private boolean completed;
}
