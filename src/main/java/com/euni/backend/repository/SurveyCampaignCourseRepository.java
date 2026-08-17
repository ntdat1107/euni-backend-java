package com.euni.backend.repository;

import com.euni.backend.entity.SurveyCampaignCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyCampaignCourseRepository extends JpaRepository<SurveyCampaignCourse, Long> {

    @Query("SELECT scc FROM SurveyCampaignCourse scc JOIN FETCH scc.course WHERE scc.campaign.id = :campaignId AND scc.deleted = false")
    List<SurveyCampaignCourse> findByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT scc FROM SurveyCampaignCourse scc JOIN FETCH scc.course WHERE scc.campaign.id = :campaignId AND scc.course.id = :courseId AND scc.deleted = false")
    Optional<SurveyCampaignCourse> findByCampaignIdAndCourseId(@Param("campaignId") Long campaignId, @Param("courseId") Long courseId);

    boolean existsByCampaignIdAndCourseId(Long campaignId, Long courseId);
}
