package com.arc.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carry_forward_embossing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarryForwardEmbossing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String partNo;

    @Column(nullable = false, length = 50)
    private String serialNo;

    @Column(nullable = false, length = 20)
    private String status; // Pending, Queued, Completed

    @Column(nullable = false, length = 50)
    private String remainingSince;

    @Column(nullable = false, length = 50)
    private String nextShift;

    @Column(nullable = false, length = 50)
    private String action;
}
