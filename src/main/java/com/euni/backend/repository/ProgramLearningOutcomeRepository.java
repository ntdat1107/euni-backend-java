package com.euni.backend.repository;

import com.euni.backend.entity.ProgramLearningOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramLearningOutcomeRepository extends JpaRepository<ProgramLearningOutcome, Long> {
    @Query("SELECT plo FROM ProgramLearningOutcome plo WHERE plo.program.id = :programId AND plo.deleted = false ORDER BY plo.orderIndex ASC")
    List<ProgramLearningOutcome> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT plo FROM ProgramLearningOutcome plo WHERE plo.id = :id AND plo.deleted = false")
    Optional<ProgramLearningOutcome> findActiveById(@Param("id") Long id);

    Optional<ProgramLearningOutcome> findByProgramIdAndCode(Long programId, String code);

    boolean existsByProgramIdAndCode(Long programId, String code);
}
