package com.euni.backend.repository;

import com.euni.backend.entity.ProgramCourse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramCourseRepository extends JpaRepository<ProgramCourse, UUID> {
    @EntityGraph(attributePaths = {"course"})
    List<ProgramCourse> findAllByProgramId(UUID programId);

    Optional<ProgramCourse> findByProgramIdAndCourseId(UUID programId, UUID courseId);

    @Query(value = "SELECT * FROM program_courses WHERE program_id = :programId AND course_id = :courseId", nativeQuery = true)
    Optional<ProgramCourse> findByProgramIdAndCourseIdIncludingDeleted(UUID programId, UUID courseId);

    @Modifying
    @Query("UPDATE ProgramCourse pc SET pc.deleted = true WHERE pc.program.id = :programId")
    void softDeleteByProgramId(UUID programId);

    @Modifying
    @Query(value = "UPDATE program_courses SET is_deleted = false WHERE program_id = :programId AND course_id = :courseId", nativeQuery = true)
    void restoreByProgramIdAndCourseId(UUID programId, UUID courseId);

    @Modifying
    @Query("UPDATE ProgramCourse pc SET pc.deleted = true WHERE pc.program.id = :programId")
    void deleteByProgramId(UUID programId);
}
