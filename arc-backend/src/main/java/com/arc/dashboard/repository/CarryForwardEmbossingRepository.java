package com.arc.dashboard.repository;

import com.arc.dashboard.entity.CarryForwardEmbossing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarryForwardEmbossingRepository extends JpaRepository<CarryForwardEmbossing, Long> {
    List<CarryForwardEmbossing> findByStatusNotIgnoreCase(String status);
}
