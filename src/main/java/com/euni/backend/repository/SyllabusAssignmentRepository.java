package com.euni.backend.repository;

import com.euni.backend.entity.SyllabusAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusAssignmentRepository extends JpaRepository<SyllabusAssignment, Long> {
    List<SyllabusAssignment> findAllByProgramCourseIdAndDeletedFalse(Long programCourseId);
    Optional<SyllabusAssignment> findByProgramCourseIdAndUserIdAndDeletedFalse(Long programCourseId, Long userId);
}
