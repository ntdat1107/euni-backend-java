package com.euni.backend.repository;

import com.euni.backend.entity.SurveyForm;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyFormRepository extends JpaRepository<SurveyForm, Long> {
    
    @EntityGraph(attributePaths = {"program", "campaignStep"})
    Optional<SurveyForm> findByPublicSlugAndDeletedFalse(String publicSlug);

    @EntityGraph(attributePaths = {"program", "campaignStep"})
    Optional<SurveyForm> findByCampaignStepIdAndDeletedFalse(Long campaignStepId);

    List<SurveyForm> findAllByProgramIdAndDeletedFalse(Long programId);

    boolean existsByPublicSlug(String publicSlug);
}
