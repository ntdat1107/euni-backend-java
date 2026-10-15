package com.euni.backend.repository;

import com.euni.backend.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
    List<Syllabus> findByProgramCourseIdAndDeletedFalse(Long programCourseId);
    Optional<Syllabus> findFirstByProgramCourseIdAndDeletedFalseOrderByVersionDesc(Long programCourseId);
    Optional<Syllabus> findByProgramCourseIdAndVersionAndDeletedFalse(Long programCourseId, String version);
}
