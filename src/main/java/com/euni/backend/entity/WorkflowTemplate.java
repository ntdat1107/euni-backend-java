package com.euni.backend.entity;

import com.euni.backend.entity.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_templates_id_seq")
    @SequenceGenerator(name = "workflow_templates_id_seq", sequenceName = "workflow_templates_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "json_content", nullable = false, columnDefinition = "TEXT")
    private String jsonContent;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.ACTIVE;
}
