package com.euni.backend.repository;

import com.euni.backend.entity.SurveySubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, Long> {
    
    @EntityGraph(attributePaths = {"answers", "answers.question"})
    List<SurveySubmission> findAllByFormIdAndDeletedFalseOrderBySubmittedAtDesc(Long formId);

    long countByFormIdAndDeletedFalse(Long formId);
}
