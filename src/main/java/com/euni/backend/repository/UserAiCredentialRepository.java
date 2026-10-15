package com.euni.backend.repository;

import com.euni.backend.entity.UserAiCredential;
import com.euni.backend.entity.enums.AiProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAiCredentialRepository extends JpaRepository<UserAiCredential, Long> {

    @Query("SELECT c FROM UserAiCredential c WHERE c.user.id = :userId AND c.deleted = false ORDER BY c.createdAt DESC")
    List<UserAiCredential> findByUserIdAndDeletedFalse(@Param("userId") Long userId);

    @Query("SELECT c FROM UserAiCredential c WHERE c.user.id = :userId AND c.active = true AND c.deleted = false ORDER BY c.createdAt DESC")
    List<UserAiCredential> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM UserAiCredential c WHERE c.id = :id AND c.user.id = :userId AND c.deleted = false")
    Optional<UserAiCredential> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM UserAiCredential c WHERE c.user.id = :userId AND c.provider = :provider AND c.active = true AND c.deleted = false")
    Optional<UserAiCredential> findFirstActiveByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") AiProvider provider);
}
