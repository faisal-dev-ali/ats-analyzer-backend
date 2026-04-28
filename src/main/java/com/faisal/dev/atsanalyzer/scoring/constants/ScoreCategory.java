package com.faisal.dev.atsanalyzer.scoring.constants;

import com.faisal.dev.atsanalyzer.entity.SuggestionCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScoreCategory {

    ATS_COMPATIBILITY(
            "ats-compatibility",
            "ATS Compatibility",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.ATS_COMPATIBILITY
    ),
    IMPACT_METRICS(
            "impact-metrics",
            "Impact & Metrics",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.IMPACT
    ),
    READABILITY(
            "readability",
            "Readability",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.READABILITY
    ),
    ACTION_VERBS(
            "action-verbs",
            "Action-Oriented Writing",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.ACTION_VERBS
    ),
    CONTENT_CLARITY(
            "content-clarity",
            "Content Clarity",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.CONTENT_CLARITY
    ),
    RESUME_LENGTH(
            "resume-length",
            "Resume Length",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.RESUME_LENGTH
    ),
    STRUCTURE_QUALITY(
            "structure-quality",
            "Structure Quality",
            ScoreLayer.UNIVERSAL,
            SuggestionCategory.STRUCTURE
    ),
    DOMAIN_RELEVANCE(
            "domain-relevance",
            "Domain Relevance",
            ScoreLayer.DOMAIN,
            SuggestionCategory.DOMAIN_RELEVANCE
    );

    private final String key;

    private final String label;

    private final ScoreLayer layer;

    private final SuggestionCategory suggestionCategory;
}
