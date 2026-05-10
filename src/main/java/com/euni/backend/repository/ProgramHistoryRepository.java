package com.euni.backend.repository;

import com.euni.backend.entity.history.ProgramHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgramHistoryRepository extends JpaRepository<ProgramHistory, UUID> {
    List<ProgramHistory> findByProgramIdOrderByRevisionNumberDesc(UUID programId);
}
