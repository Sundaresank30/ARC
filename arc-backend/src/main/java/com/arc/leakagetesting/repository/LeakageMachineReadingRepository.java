package com.arc.leakagetesting.repository;

import com.arc.leakagetesting.entity.LeakageMachineReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeakageMachineReadingRepository extends JpaRepository<LeakageMachineReading, Long> {

    List<LeakageMachineReading> findByReadingStatusOrderByIdAsc(String readingStatus);

    Optional<LeakageMachineReading> findFirstByReadingStatusOrderByIdAsc(String readingStatus);

    Optional<LeakageMachineReading> findFirstBySerialNumberAndPartNumberAndReadingStatusOrderByIdAsc(
            String serialNumber, String partNumber, String readingStatus);

    Optional<LeakageMachineReading> findFirstByBatchIdAndReadingStatusOrderByIdAsc(String batchId, String readingStatus);

    long countByReadingStatus(String readingStatus);
}
