package com.arc.machine.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbossingQueueDto {

    private Long id;
    private String partNumber;
    private String serialNumber;
    private String status;
    private LocalDateTime printedAt;
    private LocalDate printedDate;
}
