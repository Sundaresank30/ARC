package com.arc.leakagetesting.repository;

import com.arc.leakagetesting.entity.FailedLeakageTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FailedLeakageTestResultRepository extends JpaRepository<FailedLeakageTestResult, Long> {

    List<FailedLeakageTestResult> findByBatchIdOrderByIdAsc(String batchId);

    Optional<FailedLeakageTestResult> findFirstBySerialNumberAndPartNumberOrderByIdDesc(String serialNumber, String partNumber);

    long countByBatchId(String batchId);
}
