package com.faisal.dev.atsanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ats_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtsScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    private Double overallScore;

    private Double universalScore;

    private Double domainRelevanceScore;

    private Double atsCompatibilityScore;

    private Double technicalSkillsScore;

    private Double impactMetricsScore;

    private Double readabilityScore;

    private Double keywordDensityScore;

    private Double actionVerbScore;

    private Double contentClarityScore;

    private Double resumeLengthScore;

    private Double structureQualityScore;

    private String detectedDomain;

    private String inferredSeniority;

    private Integer inferredExperienceYears;

    private LocalDateTime createdAt;
}
