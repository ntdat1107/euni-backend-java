package com.euni.backend.repository;

import com.euni.backend.entity.CloPiMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloPiMapRepository extends JpaRepository<CloPiMap, Long> {
    List<CloPiMap> findAllByCloIdAndDeletedFalse(Long cloId);

    @Query("SELECT m FROM CloPiMap m WHERE m.clo.syllabus.id = :syllabusId AND m.deleted = false")
    List<CloPiMap> findAllBySyllabusId(@Param("syllabusId") Long syllabusId);

    @Query("SELECT m FROM CloPiMap m WHERE m.clo.syllabus.programCourse.program.id = :programId AND m.deleted = false")
    List<CloPiMap> findAllByProgramId(@Param("programId") Long programId);

    Optional<CloPiMap> findByCloIdAndPerformanceIndicatorId(Long cloId, Long piId);

    void deleteAllByCloId(Long cloId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CloPiMap m WHERE m.performanceIndicator.id = :piId")
    void deleteAllByPerformanceIndicatorId(@Param("piId") Long piId);
}
