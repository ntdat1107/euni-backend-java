package com.euni.backend.repository;

import com.euni.backend.entity.WorkflowTemplateDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateDraftRepository extends JpaRepository<WorkflowTemplateDraft, UUID> {
    @Query("SELECT d FROM WorkflowTemplateDraft d WHERE d.templateId = :templateId AND d.deleted = false")
    Optional<WorkflowTemplateDraft> findByTemplateId(@Param("templateId") UUID templateId);

    @Query("SELECT d FROM WorkflowTemplateDraft d WHERE d.code = :code AND d.deleted = false")
    Optional<WorkflowTemplateDraft> findByCode(@Param("code") String code);

    @Query("SELECT d FROM WorkflowTemplateDraft d WHERE d.id = :id AND d.deleted = false")
    Optional<WorkflowTemplateDraft> findActiveById(@Param("id") UUID id);
}
