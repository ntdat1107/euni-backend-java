package com.euni.backend.repository;

import com.euni.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    @Query("SELECT p FROM Permission p WHERE p.deleted = false")
    List<Permission> findAllActive();

    @Query("SELECT p FROM Permission p WHERE p.code = :code AND p.deleted = false")
    Optional<Permission> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.deleted = false")
    Optional<Permission> findActiveById(@Param("id") UUID id);
}
