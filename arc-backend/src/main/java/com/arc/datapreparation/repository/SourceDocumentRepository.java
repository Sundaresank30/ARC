package com.arc.datapreparation.repository;

import com.arc.datapreparation.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {
    List<SourceDocument> findByBatchId(String batchId);
}
