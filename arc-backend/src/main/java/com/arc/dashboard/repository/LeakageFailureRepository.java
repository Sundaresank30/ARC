package com.arc.dashboard.repository;

import com.arc.dashboard.entity.LeakageFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface LeakageFailureRepository extends JpaRepository<LeakageFailure, Long> {
    List<LeakageFailure> findByStatusIgnoreCase(String status);
    Optional<LeakageFailure> findByPartNoAndSerialNo(String partNo, String serialNo);
    boolean existsByPartNoAndSerialNo(String partNo, String serialNo);
}
