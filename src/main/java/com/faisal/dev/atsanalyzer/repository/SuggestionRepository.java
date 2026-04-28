package com.faisal.dev.atsanalyzer.repository;

import com.faisal.dev.atsanalyzer.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    void deleteByResumeId(Long id);

    List<Suggestion> findByResumeIdOrderByCreatedAtAsc(Long resumeId);
}
