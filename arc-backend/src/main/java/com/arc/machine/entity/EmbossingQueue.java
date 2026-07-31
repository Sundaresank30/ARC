package com.arc.machine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "embossing_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbossingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_number", nullable = false, length = 50)
    private String partNumber;

    @Column(name = "serial_number", nullable = false, length = 50)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmbossingQueueStatus status;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "printed_date")
    private LocalDate printedDate;
}
