package com.euni.backend.repository;

import com.euni.backend.entity.ProgramElectiveGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramElectiveGroupRepository extends JpaRepository<ProgramElectiveGroup, Long> {
    @Query("SELECT eg FROM ProgramElectiveGroup eg WHERE eg.structure.id = :structureId AND eg.deleted = false")
    List<ProgramElectiveGroup> findByStructureId(@Param("structureId") Long structureId);

    @Query("SELECT eg FROM ProgramElectiveGroup eg WHERE eg.id = :id AND eg.deleted = false")
    Optional<ProgramElectiveGroup> findActiveById(@Param("id") Long id);

    Optional<ProgramElectiveGroup> findByStructureIdAndName(Long structureId, String name);
}
