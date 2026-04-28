package com.faisal.dev.atsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeAnalysisResponse(
        Long resumeId,
        boolean analysisAvailable,
        String failureReason,
        ResumeAnalysisHeaderResponse header,
        List<ResumeScoreCardResponse> scoreCards,
        ResumeKeywordOverviewResponse keywordOverview,
        List<ResumeHighlightResponse> strengths,
        List<ResumeSuggestionItemResponse> suggestions,
        List<ResumeScoreBreakdownResponse> scoreBreakdown
) {
}
