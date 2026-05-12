package com.euni.backend.repository;

import com.euni.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    @Query("SELECT c FROM Course c WHERE c.deleted = false")
    List<Course> findAllActive();

    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.code = :code AND c.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT c FROM Course c WHERE c.id = :id AND c.deleted = false")
    Optional<Course> findActiveById(@Param("id") UUID id);
}
