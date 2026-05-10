package com.euni.backend.repository;

import com.euni.backend.entity.Major;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MajorRepository extends JpaRepository<Major, UUID> {
    @EntityGraph(attributePaths = {"faculty"})
    List<Major> findAll();

    boolean existsByCode(String code);

    Optional<Major> findByCode(String code);
}
