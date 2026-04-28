package com.faisal.dev.atsanalyzer.scoring.universal;

import com.faisal.dev.atsanalyzer.scoring.config.AtsScoringProperties;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import com.faisal.dev.atsanalyzer.scoring.scorer.ResumeScorer;
import com.faisal.dev.atsanalyzer.scoring.utils.RegexUtils;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(7)
@RequiredArgsConstructor
public class StructureQualityScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        double score = 100.0d;
        List<String> findings = new ArrayList<>();
        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();

        LinkedHashMap<String, Integer> sectionPositions =
                TextAnalysisUtils.sectionPositions(
                        context.normalizedText(),
                        scoringProperties.getStandardSections()
                );

        List<String> missingSections = List.of(
                "experience",
                "skills",
                "education"
        ).stream()
                .filter(section ->
                        !sectionPositions.containsKey(section)
                )
                .toList();

        if (!missingSections.isEmpty()) {
            score -= missingSections.size() * 12.0d;
            findings.add(
                    "Important resume sections are missing or not clearly labeled."
            );
            recommendations.add(
                    "Add clear headings for " +
                            String.join(", ", missingSections) +
                            " so the resume follows a standard structure."
            );
        } else {
            highlights.add(
                    "The resume uses recognizable section blocks."
            );
        }

        if (!TextAnalysisUtils.hasContactInfo(
                context.cleanedText()
        )) {
            score -= 15.0d;
            findings.add(
                    "Contact information is incomplete or hard to detect."
            );
            recommendations.add(
                    "Place email plus phone or LinkedIn near the top of the resume in plain text."
            );
        } else {
            highlights.add(
                    "Contact information is easy to detect."
            );
        }

        if (context.totalWords() > 140 &&
                context.bulletLines().size() < 4) {
            score -= 18.0d;
            findings.add(
                    "The structure does not use enough bullets for achievement-heavy sections."
            );
            recommendations.add(
                    "Use bullets for experience and project entries so achievements are grouped cleanly."
            );
        }

        if (!RegexUtils.YEAR_PATTERN.matcher(
                context.cleanedText()
        ).find()) {
            score -= 10.0d;
            findings.add(
                    "Timeline signals are limited, which makes chronology harder to follow."
            );
            recommendations.add(
                    "Include dates for experience and education entries to make progression clear."
            );
        } else {
            highlights.add(
                    "Timeline markers help the resume feel organized."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.STRUCTURE_QUALITY)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.STRUCTURE_QUALITY
                ))
                .explanation(findings.isEmpty()
                        ? "The resume structure is organized with clear sections, visible contact details, and scannable entries."
                        : String.join(" ", findings))
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
