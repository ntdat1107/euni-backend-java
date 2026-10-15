package com.euni.backend.repository;

import com.euni.backend.entity.SurveySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {
    List<SurveySection> findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(Long formId);
}
