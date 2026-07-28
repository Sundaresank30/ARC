package com.arc.dataembossing.repository;

import com.arc.dataembossing.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByBatchIdOrderByTimestampDesc(Long batchId);
}
