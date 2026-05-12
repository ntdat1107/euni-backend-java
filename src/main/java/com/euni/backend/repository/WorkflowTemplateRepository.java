package com.euni.backend.repository;

import com.euni.backend.entity.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, UUID> {
    @Query("SELECT t FROM WorkflowTemplate t WHERE t.code = :code AND t.deleted = false ORDER BY t.version DESC")
    List<WorkflowTemplate> findByCodeOrderByVersionDesc(@Param("code") String code);

    @Query("SELECT t FROM WorkflowTemplate t WHERE t.code = :code AND t.deleted = false ORDER BY t.version DESC")
    Optional<WorkflowTemplate> findFirstByCodeOrderByVersionDesc(@Param("code") String code);

    @Query("SELECT t FROM WorkflowTemplate t WHERE t.code = :code AND t.deleted = false ORDER BY t.version DESC LIMIT 1")
    Optional<WorkflowTemplate> findTopByCodeOrderByVersionDesc(@Param("code") String code);

    @Query("SELECT t FROM WorkflowTemplate t WHERE t.id = :id AND t.deleted = false")
    Optional<WorkflowTemplate> findActiveById(@Param("id") UUID id);


    @Query(value = "SELECT DISTINCT ON (code) * FROM workflow_templates WHERE is_deleted = false ORDER BY code, version DESC", nativeQuery = true)
    List<WorkflowTemplate> findLatestTemplates();

    @Query(value = "SELECT COALESCE(MAX(version), 0) FROM workflow_templates WHERE code = :code", nativeQuery = true)
    int getMaxVersionIncludingDeleted(@Param("code") String code);

    @Query(value = "SELECT * FROM workflow_templates WHERE code = :code ORDER BY version DESC", nativeQuery = true)
    List<WorkflowTemplate> findAllVersionsByCode(@Param("code") String code);

    @Query(value = "SELECT * FROM workflow_templates WHERE id = :id", nativeQuery = true)
    Optional<WorkflowTemplate> findByIdIncludingDeleted(@Param("id") UUID id);
}
