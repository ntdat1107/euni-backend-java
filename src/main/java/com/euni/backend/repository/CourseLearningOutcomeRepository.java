package com.euni.backend.repository;

import com.euni.backend.entity.CourseLearningOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseLearningOutcomeRepository extends JpaRepository<CourseLearningOutcome, Long> {
    List<CourseLearningOutcome> findAllBySyllabusIdAndDeletedFalseOrderByOrderIndexAsc(Long syllabusId);
    Optional<CourseLearningOutcome> findBySyllabusIdAndCodeAndDeletedFalse(Long syllabusId, String code);
    void deleteAllBySyllabusId(Long syllabusId);
}
