package com.euni.backend.repository;

import com.euni.backend.entity.WorkflowTemplateDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateDraftRepository extends JpaRepository<WorkflowTemplateDraft, UUID> {
    Optional<WorkflowTemplateDraft> findByTemplateId(UUID templateId);
    Optional<WorkflowTemplateDraft> findByCode(String code);
}
