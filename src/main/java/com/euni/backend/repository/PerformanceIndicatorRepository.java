package com.euni.backend.repository;

import com.euni.backend.entity.PerformanceIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceIndicatorRepository extends JpaRepository<PerformanceIndicator, Long> {
    @Query("SELECT pi FROM PerformanceIndicator pi WHERE pi.outcome.id = :outcomeId AND pi.deleted = false ORDER BY pi.orderIndex ASC")
    List<PerformanceIndicator> findAllByOutcomeId(@Param("outcomeId") Long outcomeId);

    @Query("SELECT pi FROM PerformanceIndicator pi WHERE pi.outcome.program.id = :programId AND pi.deleted = false ORDER BY pi.outcome.orderIndex ASC, pi.orderIndex ASC")
    List<PerformanceIndicator> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT pi FROM PerformanceIndicator pi WHERE pi.id = :id AND pi.deleted = false")
    Optional<PerformanceIndicator> findActiveById(@Param("id") Long id);

    Optional<PerformanceIndicator> findByOutcomeIdAndCode(Long outcomeId, String code);

    boolean existsByOutcomeIdAndCode(Long outcomeId, String code);
}
