package com.euni.backend.entity;

import com.euni.backend.entity.enums.AiProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_ai_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAiCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_ai_credentials_id_seq")
    @SequenceGenerator(name = "user_ai_credentials_id_seq", sequenceName = "user_ai_credentials_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AiProvider provider;

    @Column(name = "key_alias", nullable = false, length = 100)
    private String keyAlias;

    @Column(name = "encrypted_api_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedApiKey;

    @Column(name = "encryption_iv", nullable = false, length = 100)
    private String encryptionIv;

    @Column(name = "base_url", length = 255)
    private String baseUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
