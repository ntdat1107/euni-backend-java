package com.euni.backend.repository;

import com.euni.backend.entity.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {
    List<SurveyQuestionOption> findAllByQuestionIdAndDeletedFalseOrderByOrderIndexAsc(Long questionId);
}
