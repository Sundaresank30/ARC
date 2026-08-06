package com.arc.leakagetesting.repository;

import com.arc.leakagetesting.entity.LeakageTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeakageTestResultRepository extends JpaRepository<LeakageTestResult, Long> {

    List<LeakageTestResult> findByBatchIdOrderByIdAsc(String batchId);

    List<LeakageTestResult> findByBatchIdAndStatusOrderByIdAsc(String batchId, String status);

    Optional<LeakageTestResult> findFirstBySerialNumberAndPartNumberOrderByIdDesc(String serialNumber, String partNumber);

    long countByBatchId(String batchId);

    long countByBatchIdAndStatus(String batchId, String status);

    long countByStatus(String status);
}
