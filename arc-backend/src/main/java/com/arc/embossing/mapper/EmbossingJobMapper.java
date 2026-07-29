package com.arc.embossing.mapper;

import com.arc.embossing.dto.EmbossingJobResponse;
import com.arc.embossing.entity.EmbossingJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbossingJobMapper {

    public EmbossingJobResponse toResponse(EmbossingJob job) {
        if (job == null) {
            return null;
        }

        return EmbossingJobResponse.builder()
                .id(job.getId())
                .batchId(job.getBatchId())
                .partNumber(job.getPartNumber())
                .serialNumber(job.getSerialNumber())
                .embossingStatus(job.getEmbossingStatus())
                .createdTime(job.getCreatedTime())
                .embossingStartTime(job.getEmbossingStartTime())
                .embossingCompletedTime(job.getEmbossingCompletedTime())
                .machineStatus(job.getMachineStatus())
                .remarks(job.getRemarks())
                .build();
    }

    public List<EmbossingJobResponse> toResponseList(List<EmbossingJob> jobs) {
        return jobs.stream()
                .map(this::toResponse)
                .toList();
    }
}
