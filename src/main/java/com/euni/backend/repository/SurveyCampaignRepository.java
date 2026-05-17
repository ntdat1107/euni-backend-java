package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyCampaignRepository extends JpaRepository<SurveyCampaign, UUID> {
    @EntityGraph(attributePaths = {"program", "workflowTemplate", "steps"})
    Optional<SurveyCampaign> findById(UUID id);

    @EntityGraph(attributePaths = {"program", "workflowTemplate"})
    List<SurveyCampaign> findAll();

    Optional<SurveyCampaign> findByCode(String code);
}
