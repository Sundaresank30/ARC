package com.arc.embossing.repository;

import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmbossingJobRepository extends JpaRepository<EmbossingJob, Long> {

    long countByEmbossingStatus(EmbossingStatus embossingStatus);

    long countByBatchIdAndEmbossingStatus(String batchId, EmbossingStatus embossingStatus);

    List<EmbossingJob> findByEmbossingStatusOrderByIdAsc(EmbossingStatus embossingStatus);

    List<EmbossingJob> findByBatchIdOrderByIdAsc(String batchId);

    Optional<EmbossingJob> findFirstByEmbossingStatusInOrderByIdAsc(List<EmbossingStatus> statuses);

    Optional<EmbossingJob> findFirstByEmbossingStatusOrderByIdAsc(EmbossingStatus embossingStatus);

    boolean existsByPartNumber(String partNumber);

    boolean existsBySerialNumber(String serialNumber);
}
