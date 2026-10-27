package com.euni.backend.repository;

import com.euni.backend.entity.CourseDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseDependencyRepository extends JpaRepository<CourseDependency, Long> {
    List<CourseDependency> findAllByProgramIdAndDeletedFalse(Long programId);
    List<CourseDependency> findAllByProgramIdAndCourseIdAndDeletedFalse(Long programId, Long courseId);
    void deleteAllByProgramId(Long programId);
}
