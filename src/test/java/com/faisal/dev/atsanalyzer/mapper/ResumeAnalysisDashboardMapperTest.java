package com.faisal.dev.atsanalyzer.mapper;

import com.faisal.dev.atsanalyzer.dto.DashboardCategory;
import com.faisal.dev.atsanalyzer.dto.DashboardColor;
import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisResponse;
import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.entity.ResumeAnalysis;
import com.faisal.dev.atsanalyzer.entity.Suggestion;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityLevel;
import com.faisal.dev.atsanalyzer.scoring.domain.ResumeInsights;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeAnalysisDashboardMapperTest {

    private final ResumeAnalysisDashboardMapper mapper =
            new ResumeAnalysisDashboardMapper();

    @Test
    void buildsFrontendFriendlyDashboardResponse() {

        Resume resume = Resume.builder()
                .id(10L)
                .originalFileName("resume.pdf")
                .processingStatus("COMPLETED")
                .uploadedAt(LocalDateTime.of(
                        2026,
                        4,
                        29,
                        9,
                        0
                ))
                .processedAt(LocalDateTime.of(
                        2026,
                        4,
                        29,
                        9,
                        1
                ))
                .build();

        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .totalPages(1)
                .totalWords(520)
                .totalCharacters(3800)
                .build();

        DetailedScoreResult scoreResult =
                DetailedScoreResult.builder()
                        .overallScore(76.03d)
                        .universalScore(79.10d)
                        .domainRelevanceScore(63.20d)
                        .categoryScores(List.of(
                                ScoreResult.builder()
                                        .category(
                                                ScoreCategory.ATS_COMPATIBILITY
                                        )
                                        .score(100.0d)
                                        .weight(0.14d)
                                        .explanation(
                                                "Resume formatting looks ATS-friendly."
                                        )
                                        .highlights(List.of(
                                                "Formatting stays mostly machine-readable."
                                        ))
                                        .build(),
                                ScoreResult.builder()
                                        .category(
                                                ScoreCategory.IMPACT_METRICS
                                        )
                                        .score(52.0d)
                                        .weight(0.16d)
                                        .explanation(
                                                "The resume needs more measurable outcomes."
                                        )
                                        .recommendations(List.of(
                                                "Add measurable outcomes to recent bullet points using percentages, revenue, scale, speed, volume, or throughput."
                                        ))
                                        .build()
                        ))
                        .strengths(List.of(
                                "Formatting stays mostly machine-readable."
                        ))
                        .improvements(List.of(
                                "Add measurable outcomes to recent bullet points using percentages, revenue, scale, speed, volume, or throughput."
                        ))
                        .resumeInsights(new ResumeInsights(
                                "software-engineering",
                                "Software Engineering",
                                0.78d,
                                SeniorityLevel.MID,
                                3,
                                List.of(
                                        "Git",
                                        "REST API",
                                        "SQL",
                                        "Docker"
                                ),
                                List.of(
                                        "Git",
                                        "REST API",
                                        "SQL"
                                ),
                                List.of("Docker"),
                                List.of(
                                        "Software Engineer",
                                        "Java",
                                        "REST API"
                                ),
                                "The resume most closely aligns with Software Engineering."
                        ))
                        .build();

        List<Suggestion> suggestions = List.of(
                Suggestion.builder()
                        .category("IMPACT")
                        .severity("HIGH")
                        .message(
                                "Add measurable outcomes to recent bullet points using percentages, revenue, scale, speed, volume, or throughput."
                        )
                        .build()
        );

        ResumeAnalysisResponse response =
                mapper.toResponse(
                        resume,
                        analysis,
                        scoreResult,
                        suggestions
                );

        assertTrue(response.analysisAvailable());
        assertEquals(
                "Good ATS Compatibility",
                response.header().overallStatus()
        );
        assertEquals(
                DashboardColor.BLUE,
                response.header().overallColor()
        );
        assertEquals(2, response.scoreCards().size());
        assertEquals(
                DashboardCategory.ATS_COMPATIBILITY,
                response.scoreCards().get(0).category()
        );
        assertEquals(3, response.keywordOverview().matchedKeywords());
        assertEquals(1, response.keywordOverview().missingKeywords());
        assertEquals(
                "Add measurable outcomes",
                response.suggestions().get(0).title()
        );
        assertFalse(response.scoreBreakdown().isEmpty());
    }

    @Test
    void buildsPendingResponseForUnprocessedResume() {

        Resume resume = Resume.builder()
                .id(11L)
                .originalFileName("resume.pdf")
                .processingStatus("PROCESSING")
                .uploadedAt(LocalDateTime.now())
                .build();

        ResumeAnalysisResponse response =
                mapper.toUnavailableResponse(
                        resume,
                        List.of()
                );

        assertFalse(response.analysisAvailable());
        assertEquals(
                "Analysis In Progress",
                response.header().overallStatus()
        );
        assertEquals(
                DashboardColor.SLATE,
                response.header().overallColor()
        );
        assertTrue(response.scoreCards().isEmpty());
    }
}
