package com.arc.dataembossing.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_name", nullable = false)
    private String machineName;

    @Column(name = "machine_code", unique = true, nullable = false)
    private String machineCode;

    @Column(nullable = false)
    private String status; // ONLINE, OFFLINE, BUSY, ERROR

    @Column(name = "threshold_config")
    private Double thresholdConfig;
}
