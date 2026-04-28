package com.faisal.dev.atsanalyzer.repository;

import com.faisal.dev.atsanalyzer.entity.AtsScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtsScoreRepository extends JpaRepository<AtsScore, Long> {

    Optional<AtsScore> findByResumeId(Long resumeId);
}
