package com.arc.dataembossing.repository;

import com.arc.dataembossing.entity.Batch;
import com.arc.dataembossing.entity.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByBatchId(String batchId);
    boolean existsByBatchId(String batchId);
    List<Batch> findByStatus(BatchStatus status);
}
