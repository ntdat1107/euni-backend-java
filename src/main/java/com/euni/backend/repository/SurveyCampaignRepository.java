package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyCampaignRepository extends JpaRepository<SurveyCampaign, UUID> {
    @Query("SELECT s FROM SurveyCampaign s WHERE s.deleted = false")
    List<SurveyCampaign> findAllActive();

    @Query("SELECT s FROM SurveyCampaign s WHERE s.id = :id AND s.deleted = false")
    Optional<SurveyCampaign> findActiveById(@Param("id") UUID id);

    @Query("SELECT s FROM SurveyCampaign s WHERE s.code = :code AND s.deleted = false")
    Optional<SurveyCampaign> findByCode(@Param("code") String code);
}
