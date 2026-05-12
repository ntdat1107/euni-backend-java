package com.euni.backend.repository;

import com.euni.backend.entity.Major;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MajorRepository extends JpaRepository<Major, UUID> {
    @EntityGraph(attributePaths = {"faculty"})
    @Query("SELECT m FROM Major m WHERE m.deleted = false")
    List<Major> findAllActive();

    @Query("SELECT COUNT(m) > 0 FROM Major m WHERE m.code = :code AND m.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT m FROM Major m WHERE m.code = :code AND m.deleted = false")
    Optional<Major> findByCode(@Param("code") String code);

    @Query("SELECT m FROM Major m WHERE m.id = :id AND m.deleted = false")
    Optional<Major> findActiveById(@Param("id") UUID id);
}
