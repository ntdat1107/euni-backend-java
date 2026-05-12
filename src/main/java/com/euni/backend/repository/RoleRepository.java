package com.euni.backend.repository;

import com.euni.backend.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT r FROM Role r WHERE r.deleted = false")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deleted = false")
    Optional<Role> findByCode(@Param("code") String code);

    @Query("SELECT r FROM Role r WHERE r.id = :id AND r.deleted = false")
    Optional<Role> findActiveById(@Param("id") UUID id);
}
