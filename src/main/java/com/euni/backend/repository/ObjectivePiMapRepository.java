package com.euni.backend.repository;

import com.euni.backend.entity.ObjectivePiMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectivePiMapRepository extends JpaRepository<ObjectivePiMap, Long> {
    @Query("SELECT m FROM ObjectivePiMap m WHERE m.objective.program.id = :programId AND m.deleted = false")
    List<ObjectivePiMap> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT m FROM ObjectivePiMap m WHERE m.objective.id = :objectiveId AND m.performanceIndicator.id = :piId AND m.deleted = false")
    Optional<ObjectivePiMap> findByObjectiveIdAndPiId(@Param("objectiveId") Long objectiveId, @Param("piId") Long piId);

    void deleteAllByObjectiveId(Long objectiveId);
}
