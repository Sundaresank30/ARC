package com.arc.embossing.dto;

import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbossingJobResponse {

    private Long id;
    private String batchId;
    private String partNumber;
    private String serialNumber;
    private EmbossingStatus embossingStatus;
    private LocalDateTime createdTime;
    private LocalDateTime embossingStartTime;
    private LocalDateTime embossingCompletedTime;
    private MachineStatus machineStatus;
    private String remarks;
}
