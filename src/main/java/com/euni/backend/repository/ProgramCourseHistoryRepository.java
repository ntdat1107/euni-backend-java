package com.euni.backend.repository;

import com.euni.backend.entity.history.ProgramCourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramCourseHistoryRepository extends JpaRepository<ProgramCourseHistory, Long> {
}
