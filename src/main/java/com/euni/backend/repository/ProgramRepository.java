package com.euni.backend.repository;

import com.euni.backend.entity.Program;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {
    @EntityGraph(attributePaths = {"major"})
    List<Program> findAll();

    boolean existsByCode(String code);
}
