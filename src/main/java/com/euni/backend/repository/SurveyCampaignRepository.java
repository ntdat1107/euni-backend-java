package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaign;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyCampaignRepository extends JpaRepository<SurveyCampaign, Long> {
    @EntityGraph(attributePaths = {"program", "workflowTemplate", "steps"})
    Optional<SurveyCampaign> findById(Long id);

    @EntityGraph(attributePaths = {"program", "workflowTemplate"})
    List<SurveyCampaign> findAll();

    Optional<SurveyCampaign> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByProgramIdAndStatusNotIn(Long programId, Collection<SurveyCampaignStatus> statuses);

    Optional<SurveyCampaign> findFirstByProgramIdAndStatusNotIn(Long programId, Collection<SurveyCampaignStatus> statuses);
}
