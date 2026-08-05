package com.arc.datapreparation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "production_batch_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_batch_id", nullable = false)
    @JsonIgnore
    private ProductionBatch productionBatch;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "item_index", nullable = false)
    private Integer itemIndex;

    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "status", nullable = false)
    private String status;
}
