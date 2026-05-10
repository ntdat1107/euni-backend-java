package com.euni.backend.entity;

import com.euni.backend.entity.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_template_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workflow_template_drafts SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkflowTemplateDraft extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(length = 100)
    private String code;

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.ACTIVE;

    @Column(name = "xml_content", nullable = false, columnDefinition = "TEXT")
    private String xmlContent;

    @UpdateTimestamp
    @Column(name = "last_saved_at")
    private ZonedDateTime lastSavedAt;
}
