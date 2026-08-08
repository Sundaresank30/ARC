package com.arc.dashboard.repository;

import com.arc.dashboard.entity.CarryForwardEmbossing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface CarryForwardEmbossingRepository extends JpaRepository<CarryForwardEmbossing, Long> {
    List<CarryForwardEmbossing> findByStatusNotIgnoreCase(String status);
    Optional<CarryForwardEmbossing> findByPartNoAndSerialNo(String partNo, String serialNo);
    boolean existsByPartNoAndSerialNo(String partNo, String serialNo);
    void deleteByPartNoAndSerialNo(String partNo, String serialNo);
}
