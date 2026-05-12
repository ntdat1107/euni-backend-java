package com.euni.backend.repository;

import com.euni.backend.entity.Program;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {
    @EntityGraph(attributePaths = {"major"})
    @Query("SELECT p FROM Program p WHERE p.deleted = false")
    List<Program> findAllActive();

    @Query("SELECT COUNT(p) > 0 FROM Program p WHERE p.code = :code AND p.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT p FROM Program p WHERE p.id = :id AND p.deleted = false")
    Optional<Program> findActiveById(@Param("id") UUID id);
}
