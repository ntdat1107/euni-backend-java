package com.euni.backend.repository;

import com.euni.backend.entity.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, UUID> {
    List<WorkflowTemplate> findByCodeOrderByVersionDesc(String code);
    Optional<WorkflowTemplate> findFirstByCodeOrderByVersionDesc(String code);
    Optional<WorkflowTemplate> findTopByCodeOrderByVersionDesc(String code);
}
