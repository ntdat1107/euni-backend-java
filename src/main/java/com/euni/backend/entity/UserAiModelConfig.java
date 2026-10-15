package com.euni.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_ai_model_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAiModelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_ai_model_configs_id_seq")
    @SequenceGenerator(name = "user_ai_model_configs_id_seq", sequenceName = "user_ai_model_configs_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id")
    private UserAiCredential credential;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column
    @Builder.Default
    private Double temperature = 0.5;

    @Column(name = "max_tokens")
    @Builder.Default
    private Integer maxTokens = 4096;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
