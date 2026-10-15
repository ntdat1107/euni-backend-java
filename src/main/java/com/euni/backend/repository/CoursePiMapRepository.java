package com.euni.backend.repository;

import com.euni.backend.entity.CoursePiMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePiMapRepository extends JpaRepository<CoursePiMap, Long> {
    @Query("SELECT m FROM CoursePiMap m WHERE m.programCourse.program.id = :programId AND m.deleted = false")
    List<CoursePiMap> findAllByProgramId(@Param("programId") Long programId);

    @Query("SELECT m FROM CoursePiMap m WHERE m.programCourse.id = :programCourseId AND m.performanceIndicator.id = :piId AND m.deleted = false")
    Optional<CoursePiMap> findByProgramCourseIdAndPiId(@Param("programCourseId") Long programCourseId, @Param("piId") Long piId);

    @Query("SELECT m FROM CoursePiMap m WHERE m.programCourse.id = :programCourseId AND m.deleted = false")
    List<CoursePiMap> findAllByProgramCourseId(@Param("programCourseId") Long programCourseId);

    void deleteAllByProgramCourseId(Long programCourseId);
}
