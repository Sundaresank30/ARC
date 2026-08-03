package com.arc.datapreparation.repository;

import com.arc.datapreparation.entity.ProductionBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionBatchItemRepository extends JpaRepository<ProductionBatchItem, Long> {
    List<ProductionBatchItem> findByProductionBatchBatchIdOrderByItemIndexAsc(String batchId);

    List<ProductionBatchItem> findByStatusOrderByIdAsc(String status);

    List<ProductionBatchItem> findByStatusIgnoreCaseOrderByIdAsc(String status);

    Optional<ProductionBatchItem> findBySerialNumberAndPartNumber(String serialNumber, String partNumber);
}
