package com.euni.backend.repository;

import com.euni.backend.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    
    @EntityGraph(attributePaths = {"question"})
    List<SurveyAnswer> findAllBySubmissionIdAndDeletedFalse(Long submissionId);

    List<SurveyAnswer> findAllByQuestionIdAndDeletedFalse(Long questionId);
}
