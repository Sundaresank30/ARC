package com.arc.datapreparation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", unique = true, nullable = false)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @ElementCollection
    @CollectionTable(name = "batch_part_series", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "part_number_series")
    @Builder.Default
    private List<String> partNumberSeries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "batch_serial_series", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "serial_number_series")
    @Builder.Default
    private List<String> serialNumberSeries = new ArrayList<>();

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "completed_count")
    private Integer completedCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.completedCount == null) this.completedCount = 0;
        if (this.failedCount == null) this.failedCount = 0;
        if (this.status == null) this.status = BatchStatus.IN_PROGRESS;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
