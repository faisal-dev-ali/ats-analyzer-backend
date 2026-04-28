package com.faisal.dev.atsanalyzer.mapper;

import com.faisal.dev.atsanalyzer.dto.DashboardCategory;
import com.faisal.dev.atsanalyzer.dto.DashboardColor;
import com.faisal.dev.atsanalyzer.dto.DashboardIcon;
import com.faisal.dev.atsanalyzer.dto.DashboardSeverity;
import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisHeaderResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeHighlightResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeKeywordOverviewResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeScoreBreakdownResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeScoreCardResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeSuggestionItemResponse;
import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.entity.ResumeAnalysis;
import com.faisal.dev.atsanalyzer.entity.Suggestion;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ResumeAnalysisDashboardMapper {

    public ResumeAnalysisResponse toResponse(
            Resume resume,
            ResumeAnalysis analysis,
            DetailedScoreResult scoreResult,
            List<Suggestion> suggestions
    ) {

        return new ResumeAnalysisResponse(
                resume.getId(),
                true,
                null,
                buildHeader(
                        resume,
                        analysis,
                        scoreResult
                ),
                buildScoreCards(scoreResult),
                buildKeywordOverview(scoreResult),
                buildStrengths(scoreResult),
                buildSuggestions(suggestions),
                buildScoreBreakdown(scoreResult)
        );
    }

    public ResumeAnalysisResponse toUnavailableResponse(
            Resume resume,
            List<Suggestion> suggestions
    ) {

        return new ResumeAnalysisResponse(
                resume.getId(),
                false,
                resume.getFailureReason(),
                buildPendingHeader(resume),
                List.of(),
                new ResumeKeywordOverviewResponse(
                        0,
                        0,
                        0,
                        List.of(),
                        List.of()
                ),
                List.of(),
                buildSuggestions(suggestions),
                List.of()
        );
    }

    private ResumeAnalysisHeaderResponse buildHeader(
            Resume resume,
            ResumeAnalysis analysis,
            DetailedScoreResult scoreResult
    ) {

        return new ResumeAnalysisHeaderResponse(
                resume.getOriginalFileName(),
                resume.getUploadedAt(),
                resume.getProcessedAt(),
                analysis.getTotalPages(),
                analysis.getTotalWords(),
                analysis.getTotalCharacters(),
                resume.getProcessingStatus(),
                scoreResult.getOverallScore(),
                scoreResult.getUniversalScore(),
                scoreResult.getDomainRelevanceScore(),
                scoreResult.getResumeInsights()
                        .detectedDomainLabel(),
                scoreResult.getResumeInsights()
                        .seniorityLevel()
                        .getLabel(),
                scoreResult.getResumeInsights()
                        .inferredExperienceYears(),
                overallStatus(scoreResult.getOverallScore()),
                colorForScore(scoreResult.getOverallScore()),
                buildSummary(scoreResult)
        );
    }

    private ResumeAnalysisHeaderResponse buildPendingHeader(
            Resume resume
    ) {

        return new ResumeAnalysisHeaderResponse(
                resume.getOriginalFileName(),
                resume.getUploadedAt(),
                resume.getProcessedAt(),
                null,
                null,
                null,
                resume.getProcessingStatus(),
                null,
                null,
                null,
                null,
                null,
                null,
                pendingStatus(resume),
                pendingColor(resume),
                pendingSummary(resume)
        );
    }

    private List<ResumeScoreCardResponse> buildScoreCards(
            DetailedScoreResult scoreResult
    ) {

        return scoreResult.getCategoryScores()
                .stream()
                .map(this::toScoreCard)
                .toList();
    }

    private ResumeScoreCardResponse toScoreCard(
            ScoreResult scoreResult
    ) {

        DashboardCategory category =
                DashboardCategory.fromScoreCategory(
                        scoreResult.getCategory()
                );

        return new ResumeScoreCardResponse(
                scoreResult.getCategory().getKey(),
                category,
                category.getLabel(),
                scoreResult.getScore(),
                weightPercentage(scoreResult.getWeight()),
                scoreResult.getExplanation(),
                scoreStatus(scoreResult.getScore()),
                colorForScore(scoreResult.getScore()),
                iconForCategory(category)
        );
    }

    private ResumeKeywordOverviewResponse buildKeywordOverview(
            DetailedScoreResult scoreResult
    ) {

        List<String> matchedSkills = scoreResult.getResumeInsights()
                .detectedSkills();
        List<String> missingSkills = scoreResult.getResumeInsights()
                .missingSkills();

        return new ResumeKeywordOverviewResponse(
                matchedSkills.size(),
                missingSkills.size(),
                matchedSkills.size() + missingSkills.size(),
                matchedSkills,
                missingSkills
        );
    }

    private List<ResumeHighlightResponse> buildStrengths(
            DetailedScoreResult scoreResult
    ) {

        Map<String, ResumeHighlightResponse> highlights =
                new LinkedHashMap<>();

        for (ScoreResult categoryScore :
                scoreResult.getCategoryScores()) {

            DashboardCategory category =
                    DashboardCategory.fromScoreCategory(
                            categoryScore.getCategory()
                    );

            for (String highlight :
                    categoryScore.getHighlights()) {

                highlights.putIfAbsent(
                        highlight,
                        new ResumeHighlightResponse(
                                categoryScore.getCategory()
                                        .getKey() +
                                        "-" +
                                        highlights.size(),
                                category,
                                highlight,
                                DashboardColor.GREEN,
                                iconForCategory(category)
                        )
                );
            }
        }

        return highlights.values()
                .stream()
                .limit(8)
                .toList();
    }

    private List<ResumeSuggestionItemResponse> buildSuggestions(
            List<Suggestion> suggestions
    ) {

        AtomicInteger counter = new AtomicInteger(1);

        return suggestions.stream()
                .map(suggestion ->
                        toSuggestion(
                                suggestion,
                                counter.getAndIncrement()
                        )
                )
                .toList();
    }

    private ResumeSuggestionItemResponse toSuggestion(
            Suggestion suggestion,
            int index
    ) {

        DashboardCategory category =
                DashboardCategory.fromSuggestionCategory(
                        suggestion.getCategory()
                );
        DashboardSeverity severity =
                DashboardSeverity.fromValue(
                        suggestion.getSeverity()
                );

        return new ResumeSuggestionItemResponse(
                suggestion.getId() != null
                        ? suggestion.getId().toString()
                        : category.name().toLowerCase(Locale.ROOT) +
                        "-" +
                        index,
                category,
                severity,
                suggestionTitle(category, suggestion.getMessage()),
                suggestion.getMessage(),
                iconForCategory(category),
                colorForSeverity(severity)
        );
    }

    private List<ResumeScoreBreakdownResponse> buildScoreBreakdown(
            DetailedScoreResult scoreResult
    ) {

        return scoreResult.getCategoryScores()
                .stream()
                .map(this::toBreakdown)
                .toList();
    }

    private ResumeScoreBreakdownResponse toBreakdown(
            ScoreResult scoreResult
    ) {

        DashboardCategory category =
                DashboardCategory.fromScoreCategory(
                        scoreResult.getCategory()
                );

        return new ResumeScoreBreakdownResponse(
                category,
                category.getLabel(),
                scoreResult.getCategory().getLayer(),
                scoreResult.getScore(),
                weightPercentage(scoreResult.getWeight()),
                scoreStatus(scoreResult.getScore()),
                colorForScore(scoreResult.getScore()),
                iconForCategory(category),
                scoreResult.getExplanation(),
                List.copyOf(scoreResult.getHighlights()),
                List.copyOf(scoreResult.getRecommendations())
        );
    }

    private String buildSummary(
            DetailedScoreResult scoreResult
    ) {

        String prefix = switch (scoreBand(
                scoreResult.getOverallScore()
        )) {
            case EXCELLENT ->
                    "Your resume is strongly positioned for ATS screening";
            case GOOD ->
                    "Your resume is ATS friendly overall";
            case FAIR ->
                    "Your resume has a workable ATS foundation";
            case NEEDS_WORK ->
                    "Your resume needs stronger ATS optimization";
        };

        if (!scoreResult.getImprovements().isEmpty()) {
            return prefix + " but " +
                    lowerCaseFirst(
                            ensureTrailingPeriod(
                                    scoreResult.getImprovements()
                                            .get(0)
                            )
                    );
        }

        if (!scoreResult.getStrengths().isEmpty()) {
            return prefix + " and already shows strengths such as " +
                    lowerCaseFirst(
                            ensureTrailingPeriod(
                                    scoreResult.getStrengths()
                                            .get(0)
                            )
                    );
        }

        return prefix + ".";
    }

    private String pendingStatus(
            Resume resume
    ) {

        return switch (normalizeStatus(
                resume.getProcessingStatus()
        )) {
            case "COMPLETED" -> "Analysis Ready";
            case "FAILED" -> "Analysis Failed";
            case "PROCESSING" -> "Analysis In Progress";
            default -> "Awaiting Analysis";
        };
    }

    private DashboardColor pendingColor(
            Resume resume
    ) {

        return switch (normalizeStatus(
                resume.getProcessingStatus()
        )) {
            case "FAILED" -> DashboardColor.RED;
            case "COMPLETED" -> DashboardColor.GREEN;
            default -> DashboardColor.SLATE;
        };
    }

    private String pendingSummary(
            Resume resume
    ) {

        String status = normalizeStatus(
                resume.getProcessingStatus()
        );

        if ("FAILED".equals(status) &&
                resume.getFailureReason() != null) {
            return "Resume analysis could not be completed: " +
                    ensureTrailingPeriod(
                            resume.getFailureReason()
                    );
        }

        if ("PROCESSING".equals(status)) {
            return "Resume parsing and ATS scoring are still running.";
        }

        return "Resume upload is complete and ATS analysis will appear here once processing finishes.";
    }

    private String overallStatus(
            Double score
    ) {

        return switch (scoreBand(score)) {
            case EXCELLENT -> "Excellent ATS Readiness";
            case GOOD -> "Good ATS Compatibility";
            case FAIR -> "Moderate ATS Readiness";
            case NEEDS_WORK -> "Needs ATS Improvement";
        };
    }

    private String scoreStatus(
            Double score
    ) {

        return switch (scoreBand(score)) {
            case EXCELLENT -> "Excellent";
            case GOOD -> "Good";
            case FAIR -> "Fair";
            case NEEDS_WORK -> "Needs Work";
        };
    }

    private DashboardColor colorForScore(
            Double score
    ) {

        return switch (scoreBand(score)) {
            case EXCELLENT -> DashboardColor.GREEN;
            case GOOD -> DashboardColor.BLUE;
            case FAIR -> DashboardColor.AMBER;
            case NEEDS_WORK -> DashboardColor.RED;
        };
    }

    private DashboardColor colorForSeverity(
            DashboardSeverity severity
    ) {

        return switch (severity) {
            case HIGH -> DashboardColor.RED;
            case MEDIUM -> DashboardColor.AMBER;
            case LOW -> DashboardColor.BLUE;
        };
    }

    private DashboardIcon iconForCategory(
            DashboardCategory category
    ) {

        return switch (category) {
            case OVERALL -> DashboardIcon.TARGET;
            case ATS_COMPATIBILITY -> DashboardIcon.SHIELD;
            case IMPACT_METRICS -> DashboardIcon.CHART;
            case READABILITY -> DashboardIcon.BOOK_OPEN;
            case ACTION_VERBS -> DashboardIcon.SPARKLES;
            case CONTENT_CLARITY -> DashboardIcon.PEN_TOOL;
            case RESUME_LENGTH -> DashboardIcon.RULER;
            case STRUCTURE_QUALITY -> DashboardIcon.LAYERS;
            case DOMAIN_RELEVANCE -> DashboardIcon.BRIEFCASE;
        };
    }

    private String suggestionTitle(
            DashboardCategory category,
            String message
    ) {

        String normalizedMessage = message == null
                ? ""
                : message.toLowerCase(Locale.ROOT);

        if (normalizedMessage.contains("measurable") ||
                normalizedMessage.contains("numbers")) {
            return "Add measurable outcomes";
        }

        if (normalizedMessage.contains("skills") ||
                normalizedMessage.contains("tools") ||
                normalizedMessage.contains("platforms")) {
            return "Surface clearer role-specific skills";
        }

        if (normalizedMessage.contains("action verbs")) {
            return "Use stronger action verbs";
        }

        if (normalizedMessage.contains("shorten") ||
                normalizedMessage.contains("readability") ||
                normalizedMessage.contains("split long")) {
            return "Tighten readability";
        }

        if (normalizedMessage.contains("section") ||
                normalizedMessage.contains("headings")) {
            return "Improve resume structure";
        }

        return switch (category) {
            case ATS_COMPATIBILITY -> "Improve ATS formatting";
            case IMPACT_METRICS -> "Strengthen impact evidence";
            case READABILITY -> "Improve readability";
            case ACTION_VERBS -> "Use stronger action verbs";
            case CONTENT_CLARITY -> "Clarify bullet point writing";
            case RESUME_LENGTH -> "Adjust resume length";
            case STRUCTURE_QUALITY -> "Improve resume structure";
            case DOMAIN_RELEVANCE -> "Add role-specific signals";
            case OVERALL -> "Strengthen overall resume quality";
        };
    }

    private int weightPercentage(
            Double weight
    ) {

        if (weight == null) {
            return 0;
        }

        return BigDecimal.valueOf(weight * 100.0d)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private ScoreBand scoreBand(
            Double score
    ) {

        double safeScore = score == null ? 0.0d : score;

        if (safeScore >= 85.0d) {
            return ScoreBand.EXCELLENT;
        }

        if (safeScore >= 70.0d) {
            return ScoreBand.GOOD;
        }

        if (safeScore >= 55.0d) {
            return ScoreBand.FAIR;
        }

        return ScoreBand.NEEDS_WORK;
    }

    private String normalizeStatus(
            String status
    ) {

        return Objects.requireNonNullElse(
                        status,
                        "UPLOADED"
                )
                .toUpperCase(Locale.ROOT);
    }

    private String lowerCaseFirst(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "";
        }

        return Character.toLowerCase(value.charAt(0)) +
                value.substring(1);
    }

    private String ensureTrailingPeriod(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "";
        }

        return value.endsWith(".")
                ? value
                : value + ".";
    }

    private enum ScoreBand {
        EXCELLENT,
        GOOD,
        FAIR,
        NEEDS_WORK
    }
}
