package com.euni.backend.repository;

import com.euni.backend.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    
    @EntityGraph(attributePaths = {"options", "section"})
    List<SurveyQuestion> findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(Long formId);

    List<SurveyQuestion> findAllBySectionIdAndDeletedFalseOrderByOrderIndexAsc(Long sectionId);
}
