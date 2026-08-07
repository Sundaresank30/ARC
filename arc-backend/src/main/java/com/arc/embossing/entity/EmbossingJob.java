package com.arc.embossing.entity;

import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "embossing_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbossingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String batchId;

    @Column(nullable = false, unique = true, length = 50)
    private String partNumber;

    @Column(nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmbossingStatus embossingStatus;

    @Column(nullable = false)
    private LocalDateTime createdTime;

    private LocalDateTime embossingStartTime;

    private LocalDateTime embossingCompletedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MachineStatus machineStatus;

    @Column(length = 500)
    private String remarks;

    @PrePersist
    @SuppressWarnings("unused")
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }
}
