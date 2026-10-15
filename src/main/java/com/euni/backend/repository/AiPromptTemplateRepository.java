package com.euni.backend.repository;

import com.euni.backend.entity.AiPromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, Long> {

    List<AiPromptTemplate> findAllByDeletedFalseOrderByIdAsc();

    Optional<AiPromptTemplate> findByCodeAndDeletedFalse(String code);

    Optional<AiPromptTemplate> findByCodeAndActiveTrueAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalseAndIdNot(String code, Long id);
}
