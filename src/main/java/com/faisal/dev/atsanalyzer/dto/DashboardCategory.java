package com.faisal.dev.atsanalyzer.dto;

import com.faisal.dev.atsanalyzer.entity.SuggestionCategory;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;

public enum DashboardCategory {

    OVERALL("Overall"),
    ATS_COMPATIBILITY("ATS Compatibility"),
    IMPACT_METRICS("Impact & Metrics"),
    READABILITY("Readability"),
    ACTION_VERBS("Action-Oriented Writing"),
    CONTENT_CLARITY("Content Clarity"),
    RESUME_LENGTH("Resume Length"),
    STRUCTURE_QUALITY("Structure Quality"),
    DOMAIN_RELEVANCE("Domain Relevance");

    private final String label;

    DashboardCategory(
            String label
    ) {

        this.label = label;
    }

    public String getLabel() {

        return label;
    }

    public static DashboardCategory fromScoreCategory(
            ScoreCategory scoreCategory
    ) {

        return switch (scoreCategory) {
            case ATS_COMPATIBILITY -> ATS_COMPATIBILITY;
            case IMPACT_METRICS -> IMPACT_METRICS;
            case READABILITY -> READABILITY;
            case ACTION_VERBS -> ACTION_VERBS;
            case CONTENT_CLARITY -> CONTENT_CLARITY;
            case RESUME_LENGTH -> RESUME_LENGTH;
            case STRUCTURE_QUALITY -> STRUCTURE_QUALITY;
            case DOMAIN_RELEVANCE -> DOMAIN_RELEVANCE;
        };
    }

    public static DashboardCategory fromSuggestionCategory(
            String category
    ) {

        if (category == null || category.isBlank()) {
            return OVERALL;
        }

        SuggestionCategory suggestionCategory;

        try {
            suggestionCategory = SuggestionCategory.valueOf(
                    category.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            return OVERALL;
        }

        return switch (suggestionCategory) {
            case ATS_COMPATIBILITY -> ATS_COMPATIBILITY;
            case TECHNICAL_SKILLS, KEYWORDS -> DOMAIN_RELEVANCE;
            case IMPACT -> IMPACT_METRICS;
            case READABILITY -> READABILITY;
            case STRUCTURE -> STRUCTURE_QUALITY;
            case ACTION_VERBS -> ACTION_VERBS;
            case CONTENT_CLARITY -> CONTENT_CLARITY;
            case RESUME_LENGTH -> RESUME_LENGTH;
            case DOMAIN_RELEVANCE -> DOMAIN_RELEVANCE;
        };
    }
}
