package com.euni.backend.repository;

import com.euni.backend.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    @Query("SELECT f FROM Faculty f WHERE f.deleted = false")
    List<Faculty> findAllActive();

    @Query("SELECT f FROM Faculty f WHERE f.code = :code AND f.deleted = false")
    Optional<Faculty> findByCode(@Param("code") String code);

    @Query("SELECT f FROM Faculty f WHERE f.id = :id AND f.deleted = false")
    Optional<Faculty> findActiveById(@Param("id") UUID id);
}
