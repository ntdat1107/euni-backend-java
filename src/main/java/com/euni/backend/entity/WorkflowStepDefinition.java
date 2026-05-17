package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "workflow_step_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WorkflowStepDefinition extends BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workflow_type", nullable = false)
    private String workflowType; // SURVEY_CREATE, SURVEY_UPDATE

    @Column(name = "step_code", nullable = false, unique = true)
    private String stepCode;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "type")
    private String type; // USER_TASK, SERVICE_TASK, etc.

    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments; // JSON array of strings

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
