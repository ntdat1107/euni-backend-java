package com.euni.backend.repository;

import com.euni.backend.entity.ProgramStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramStructureRepository extends JpaRepository<ProgramStructure, Long> {
    @Query("SELECT ps FROM ProgramStructure ps WHERE ps.program.id = :programId AND ps.deleted = false ORDER BY ps.orderIndex ASC")
    List<ProgramStructure> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT ps FROM ProgramStructure ps WHERE ps.program.id = :programId AND ps.parent IS NULL AND ps.deleted = false ORDER BY ps.orderIndex ASC")
    List<ProgramStructure> findRootsByProgramId(@Param("programId") Long programId);

    @Query("SELECT ps FROM ProgramStructure ps WHERE ps.id = :id AND ps.deleted = false")
    Optional<ProgramStructure> findActiveById(@Param("id") Long id);

    Optional<ProgramStructure> findByProgramIdAndCode(Long programId, String code);
}
