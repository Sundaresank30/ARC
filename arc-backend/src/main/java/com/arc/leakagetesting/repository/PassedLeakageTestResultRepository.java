package com.arc.leakagetesting.repository;

import com.arc.leakagetesting.entity.PassedLeakageTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassedLeakageTestResultRepository extends JpaRepository<PassedLeakageTestResult, Long> {

    List<PassedLeakageTestResult> findByBatchIdOrderByIdAsc(String batchId);

    Optional<PassedLeakageTestResult> findFirstBySerialNumberAndPartNumberOrderByIdDesc(String serialNumber, String partNumber);

    long countByBatchId(String batchId);
}
