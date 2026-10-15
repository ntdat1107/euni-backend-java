package com.euni.backend.repository;

import com.euni.backend.entity.UserAiModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAiModelConfigRepository extends JpaRepository<UserAiModelConfig, Long> {

    @Query("SELECT m FROM UserAiModelConfig m LEFT JOIN FETCH m.credential WHERE m.user.id = :userId AND m.deleted = false ORDER BY m.taskType ASC")
    List<UserAiModelConfig> findByUserIdAndDeletedFalse(@Param("userId") Long userId);

    @Query("SELECT m FROM UserAiModelConfig m LEFT JOIN FETCH m.credential WHERE m.user.id = :userId AND m.taskType = :taskType AND m.deleted = false")
    Optional<UserAiModelConfig> findByUserIdAndTaskType(@Param("userId") Long userId, @Param("taskType") String taskType);

    @Query("SELECT m FROM UserAiModelConfig m LEFT JOIN FETCH m.credential WHERE m.id = :id AND m.user.id = :userId AND m.deleted = false")
    Optional<UserAiModelConfig> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
