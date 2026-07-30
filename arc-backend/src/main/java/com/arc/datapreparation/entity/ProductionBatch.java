package com.arc.datapreparation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, unique = true)
    private String batchId;

    @Column(name = "part_no_series", nullable = false)
    private String partNoSeries;

    @Column(name = "part_no_count", nullable = false)
    private Integer partNoCount;

    @Column(name = "serial_no_series", nullable = false)
    private String serialNoSeries;

    @Column(name = "serial_no_count", nullable = false)
    private Integer serialNoCount;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "productionBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionBatchItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void addItem(ProductionBatchItem item) {
        items.add(item);
        item.setProductionBatch(this);
    }
}
