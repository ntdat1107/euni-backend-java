package com.euni.backend.repository;

import com.euni.backend.entity.WorkflowStepDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStepDefinitionRepository extends JpaRepository<WorkflowStepDefinition, Long> {
    
    @Query("SELECT w FROM WorkflowStepDefinition w WHERE w.deleted = false")
    List<WorkflowStepDefinition> findAllActive();

    @Query("SELECT w FROM WorkflowStepDefinition w WHERE w.workflowType = :workflowType AND w.deleted = false")
    List<WorkflowStepDefinition> findByWorkflowType(String workflowType);

    Optional<WorkflowStepDefinition> findByStepCodeAndDeletedFalse(String stepCode);
}
