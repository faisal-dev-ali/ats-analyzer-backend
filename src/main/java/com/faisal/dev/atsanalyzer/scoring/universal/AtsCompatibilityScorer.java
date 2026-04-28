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
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class AtsCompatibilityScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        double score = 100.0d;
        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();
        List<String> findings = new ArrayList<>();

        double specialCharacterRatio =
                TextAnalysisUtils.ratio(
                        (int) TextAnalysisUtils.countPatternMatches(
                                context.cleanedText(),
                                RegexUtils.SPECIAL_CHARACTER_PATTERN
                        ),
                        Math.max(
                                context.totalCharacters(),
                                1
                        )
                ) * 100.0d;

        if (specialCharacterRatio > 4.0d) {
            score -= 12.0d;
            findings.add(
                    "The resume contains a high volume of non-standard characters."
            );
            recommendations.add(
                    "Remove decorative symbols, tables, and text art so ATS parsers can read the document cleanly."
            );
        } else {
            highlights.add(
                    "Formatting stays mostly machine-readable."
            );
        }

        int longLines = TextAnalysisUtils.countLinesLongerThan(
                context.lines(),
                32
        );

        if (longLines > 3) {
            score -= 12.0d;
            findings.add(
                    "Several sections are written as dense paragraphs."
            );
            recommendations.add(
                    "Break dense paragraphs into shorter bullets or shorter lines for more reliable parsing."
            );
        } else {
            highlights.add(
                    "Text blocks are concise enough for ATS parsing."
            );
        }

        List<String> requiredSections = List.of(
                "experience",
                "skills",
                "education"
        );

        long presentRequiredSections =
                requiredSections.stream()
                        .filter(section ->
                                TextAnalysisUtils.containsAnyTerm(
                                        context.normalizedText(),
                                        scoringProperties.sectionTerms(
                                                section
                                        )
                                )
                        )
                        .count();

        int missingRequiredSections =
                requiredSections.size() -
                        (int) presentRequiredSections;

        if (missingRequiredSections > 0) {
            score -= missingRequiredSections * 12.0d;
            findings.add(
                    "Standard ATS section headings are incomplete."
            );
            recommendations.add(
                    "Use clear section headings such as Experience, Skills, and Education."
            );
        } else {
            highlights.add(
                    "Core ATS sections are clearly labeled."
            );
        }

        if (context.rawText() != null &&
                context.rawText().contains("\n\n\n\n")) {
            score -= 6.0d;
            findings.add(
                    "The document contains excessive blank spacing."
            );
            recommendations.add(
                    "Trim repeated blank lines to keep the resume parser-friendly."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.ATS_COMPATIBILITY)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.ATS_COMPATIBILITY
                ))
                .explanation(findings.isEmpty()
                        ? "Resume formatting looks ATS-friendly with standard headings and machine-readable text."
                        : String.join(" ", findings))
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
