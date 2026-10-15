package com.euni.backend.repository;

import com.euni.backend.entity.ProgramObjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramObjectiveRepository extends JpaRepository<ProgramObjective, Long> {
    @Query("SELECT po FROM ProgramObjective po WHERE po.program.id = :programId AND po.deleted = false ORDER BY po.orderIndex ASC")
    List<ProgramObjective> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT po FROM ProgramObjective po WHERE po.id = :id AND po.deleted = false")
    Optional<ProgramObjective> findActiveById(@Param("id") Long id);

    Optional<ProgramObjective> findByProgramIdAndCode(Long programId, String code);

    boolean existsByProgramIdAndCode(Long programId, String code);
}
