package com.arc.embossing.repository;

import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmbossingJobRepository extends JpaRepository<EmbossingJob, Long> {

    long countByEmbossingStatus(EmbossingStatus embossingStatus);

    long countByBatchIdAndEmbossingStatus(String batchId, EmbossingStatus embossingStatus);

    List<EmbossingJob> findByEmbossingStatusOrderByIdAsc(EmbossingStatus embossingStatus);

    List<EmbossingJob> findByBatchIdAndEmbossingStatusOrderByIdAsc(String batchId, EmbossingStatus embossingStatus);

    List<EmbossingJob> findByBatchIdOrderByIdAsc(String batchId);

    List<EmbossingJob> findByBatchIdAndEmbossingStatusOrderByIdAsc(String batchId, EmbossingStatus embossingStatus);

    long countByBatchId(String batchId);

    Optional<EmbossingJob> findFirstByEmbossingStatusInOrderByIdAsc(List<EmbossingStatus> statuses);

    Optional<EmbossingJob> findFirstByEmbossingStatusOrderByIdAsc(EmbossingStatus embossingStatus);

    Optional<EmbossingJob> findFirstByEmbossingStatusOrderByIdDesc(EmbossingStatus embossingStatus);

    Optional<EmbossingJob> findBySerialNumberAndPartNumber(String serialNumber, String partNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmbossingJob e WHERE e.serialNumber = :serialNumber AND e.partNumber = :partNumber")
    Optional<EmbossingJob> findBySerialNumberAndPartNumberForUpdate(
            @Param("serialNumber") String serialNumber,
            @Param("partNumber") String partNumber);

    boolean existsByPartNumber(String partNumber);

    boolean existsBySerialNumber(String serialNumber);
}
