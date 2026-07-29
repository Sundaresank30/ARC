package com.arc.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leakage_failures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeakageFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String partNo;

    @Column(nullable = false, length = 50)
    private String serialNo;

    @Column(nullable = false, length = 20)
    private String status; // Failed, Scrap, Pending

    @Column(nullable = false)
    private Double testValue;

    @Column(nullable = false, length = 10)
    private String direction; // up, down

    @Column(nullable = false, length = 50)
    private String timestamp;

    @Column(nullable = false, length = 20)
    private String attempt;

    @Column(nullable = false, length = 50)
    private String action;
}
