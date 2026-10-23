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
    Optional<SurveyForm> findFirstByCampaignStepIdAndDeletedFalseOrderByVersionDescIdDesc(Long campaignStepId);

    @EntityGraph(attributePaths = {"program", "campaignStep"})
    Optional<SurveyForm> findFirstByCampaignStepIdAndIsCurrentTrueAndDeletedFalseOrderByVersionDescIdDesc(Long campaignStepId);

    default Optional<SurveyForm> findByCampaignStepIdAndDeletedFalse(Long campaignStepId) {
        return findFirstByCampaignStepIdAndDeletedFalseOrderByVersionDescIdDesc(campaignStepId);
    }

    default Optional<SurveyForm> findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(Long campaignStepId) {
        return findFirstByCampaignStepIdAndIsCurrentTrueAndDeletedFalseOrderByVersionDescIdDesc(campaignStepId);
    }

    @EntityGraph(attributePaths = {"program", "campaignStep"})
    List<SurveyForm> findAllByCampaignStepIdAndDeletedFalseOrderByVersionDesc(Long campaignStepId);

    @EntityGraph(attributePaths = {"program", "campaignStep"})
    Optional<SurveyForm> findByIdAndDeletedFalse(Long id);

    List<SurveyForm> findAllByProgramIdAndDeletedFalse(Long programId);

    boolean existsByPublicSlug(String publicSlug);
}
