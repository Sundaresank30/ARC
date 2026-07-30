package com.arc.datapreparation.repository;

import com.arc.datapreparation.entity.ProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    Optional<ProductionBatch> findByBatchId(String batchId);
    boolean existsByBatchId(String batchId);
    List<ProductionBatch> findAllByOrderByCreatedAtDesc();
}
