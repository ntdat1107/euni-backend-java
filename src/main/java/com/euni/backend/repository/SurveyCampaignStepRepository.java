package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaignStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyCampaignStepRepository extends JpaRepository<SurveyCampaignStep, UUID> {
    List<SurveyCampaignStep> findByCampaignIdOrderByStepIndexAsc(UUID campaignId);
}
