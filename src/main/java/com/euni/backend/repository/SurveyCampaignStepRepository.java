package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaignStep;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyCampaignStepRepository extends JpaRepository<SurveyCampaignStep, Long> {
    
    @EntityGraph(attributePaths = {"campaign", "campaign.program"})
    Optional<SurveyCampaignStep> findById(Long id);

    List<SurveyCampaignStep> findByCampaignIdOrderByStepIndexAsc(Long campaignId);
}
