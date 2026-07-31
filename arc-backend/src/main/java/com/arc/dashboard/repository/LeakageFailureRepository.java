package com.arc.dashboard.repository;

import com.arc.dashboard.entity.LeakageFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeakageFailureRepository extends JpaRepository<LeakageFailure, Long> {
    List<LeakageFailure> findByStatusIgnoreCase(String status);
}
