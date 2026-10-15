package com.euni.backend.repository;

import com.euni.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);

    @Query("SELECT d FROM Department d WHERE d.deleted = false")
    List<Department> findAllActive();

    @Query("SELECT d FROM Department d WHERE d.id = :id AND d.deleted = false")
    Optional<Department> findActiveById(@Param("id") Long id);

    @Query("SELECT d FROM Department d WHERE d.faculty.id = :facultyId AND d.deleted = false")
    List<Department> findByFacultyId(@Param("facultyId") Long facultyId);

    boolean existsByCode(String code);
}
