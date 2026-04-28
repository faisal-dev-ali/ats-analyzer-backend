package com.faisal.dev.atsanalyzer.scoring.domain;

import com.faisal.dev.atsanalyzer.scoring.config.AtsScoringProperties;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import com.faisal.dev.atsanalyzer.scoring.scorer.ResumeScorer;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(8)
@RequiredArgsConstructor
public class DomainRelevanceScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        ResumeInsights insights = context.resumeInsights();

        int expectedSkillCount = Math.max(
                insights.expectedSkills().size(),
                1
        );
        int detectedSkillCount =
                insights.detectedSkills().size();
        int matchedExpectedSkillCount =
                expectedSkillCount -
                        insights.missingSkills().size();

        double expectedCoverageScore =
                TextAnalysisUtils.ratio(
                        Math.min(
                                matchedExpectedSkillCount,
                                expectedSkillCount
                        ),
                        expectedSkillCount
                ) * 60.0d;

        double domainSignalScore =
                TextAnalysisUtils.ratio(
                        Math.min(
                                insights.matchedSignals().size(),
                                6
                        ),
                        6
                ) * 20.0d;

        double confidenceScore =
                insights.domainConfidence() * 15.0d;

        boolean hasSkillsSection =
                TextAnalysisUtils.containsAnyTerm(
                        context.normalizedText(),
                        List.of(
                                "skills",
                                "competencies",
                                "tools",
                                "technologies"
                        )
                );

        double score = expectedCoverageScore +
                domainSignalScore +
                confidenceScore +
                (hasSkillsSection ? 5.0d : 0.0d);

        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();

        if (detectedSkillCount >= 4) {
            highlights.add(
                    "The resume explicitly surfaces role-relevant tools, skills, or terminology."
            );
        }

        if (insights.domainConfidence() >= 0.65d &&
                !insights.detectedDomainKey()
                        .equals("general-professional")) {
            highlights.add(
                    "The target career track comes through clearly from the resume language."
            );
        }

        if (insights.domainConfidence() < 0.45d) {
            recommendations.add(
                    "Add clearer role titles, domain keywords, and tools so the resume signals a more specific target track."
            );
        }

        if (!insights.missingSkills().isEmpty()) {
            recommendations.add(
                    "If you have this exposure, name more expected " +
                            insights.detectedDomainLabel() +
                            " signals such as " +
                            String.join(
                                    ", ",
                                    insights.missingSkills()
                                            .stream()
                                            .limit(4)
                                            .toList()
                            ) +
                            "."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.DOMAIN_RELEVANCE)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.DOMAIN_RELEVANCE
                ))
                .explanation(insights.explanation())
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
