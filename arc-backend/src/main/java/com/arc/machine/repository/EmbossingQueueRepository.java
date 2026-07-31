package com.arc.machine.repository;

import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmbossingQueueRepository extends JpaRepository<EmbossingQueue, Long> {

    /** Top 5 records that are NOT completed, ordered by id asc — the rolling buffer on load. */
    List<EmbossingQueue> findTop5ByStatusNotOrderByIdAsc(EmbossingQueueStatus status);

    /** Next single WAITING record to process. */
    Optional<EmbossingQueue> findFirstByStatusOrderByIdAsc(EmbossingQueueStatus status);

    /** Atomically claim the next WAITING record for printing. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmbossingQueue e WHERE e.status = :status ORDER BY e.id ASC")
    Optional<EmbossingQueue> claimFirstByStatusOrderByIdAsc(@Param("status") EmbossingQueueStatus status);

    /** Atomically fetch a specific row for update. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmbossingQueue e WHERE e.id = :id")
    Optional<EmbossingQueue> findByIdForUpdate(@Param("id") Long id);

    /** How many rows exist in any status — used for seeding guard. */
    long countByStatus(EmbossingQueueStatus status);

    /** Does a queue row already exist for this serial and part pair? */
    boolean existsBySerialNumberAndPartNumber(String serialNumber, String partNumber);

    /** Reset all rows back to WAITING and clear timestamps. */
    @Modifying
    @Query("UPDATE EmbossingQueue e SET e.status = com.arc.machine.entity.EmbossingQueueStatus.WAITING, e.printedAt = null, e.printedDate = null")
    void resetAllToWaiting();
}
