package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaignStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyCampaignStepRepository extends JpaRepository<SurveyCampaignStep, Long> {
    List<SurveyCampaignStep> findByCampaignIdOrderByStepIndexAsc(Long campaignId);
}
