package com.faisal.dev.atsanalyzer.scoring.universal;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(5)
@RequiredArgsConstructor
public class ContentClarityScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        int vaguePhraseCount =
                TextAnalysisUtils.countTermOccurrences(
                        context.normalizedText(),
                        scoringProperties.normalizedVaguePhrases()
                );

        int repeatedBulletStarts =
                repeatedBulletStarts(context);

        int firstPersonReferences =
                TextAnalysisUtils.countTermOccurrences(
                        context.normalizedText(),
                        List.of(" i ", " my ", " me ")
                );

        double score = 100.0d;
        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();
        List<String> findings = new ArrayList<>();

        if (vaguePhraseCount > 0) {
            score -= Math.min(20.0d, vaguePhraseCount * 6.0d);
            findings.add(
                    "Several statements rely on vague or generic wording."
            );
            recommendations.add(
                    "Replace broad phrases with specific responsibilities, tools, or outcomes."
            );
        } else {
            highlights.add(
                    "Most statements avoid generic filler language."
            );
        }

        if (repeatedBulletStarts >= 3) {
            score -= 12.0d;
            findings.add(
                    "Many bullets begin the same way, which makes achievements blur together."
            );
            recommendations.add(
                    "Vary bullet openings so responsibilities and achievements sound more precise."
            );
        } else if (!context.bulletLines().isEmpty()) {
            highlights.add(
                    "Bullet openings show reasonable variety."
            );
        }

        if (firstPersonReferences > 0) {
            score -= 8.0d;
            findings.add(
                    "The resume uses first-person phrasing that feels less polished for resume format."
            );
            recommendations.add(
                    "Drop first-person phrasing and keep bullets focused on actions and outcomes."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.CONTENT_CLARITY)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.CONTENT_CLARITY
                ))
                .explanation(findings.isEmpty()
                        ? "The resume generally uses specific, professional wording without much filler."
                        : String.join(" ", findings))
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }

    private int repeatedBulletStarts(
            ScoringContext context
    ) {

        Map<String, Integer> starterCounts =
                new LinkedHashMap<>();

        context.bulletLines().forEach(line -> {
            String normalizedLine =
                    TextAnalysisUtils.normalizeText(
                            line.replaceFirst(
                                    "^\\s*(?:[-*•]|\\d+[.)])\\s*",
                                    ""
                            )
                    );

            if (normalizedLine.isBlank()) {
                return;
            }

            String starter = normalizedLine
                    .split("\\s+")[0];
            starterCounts.merge(
                    starter,
                    1,
                    Integer::sum
            );
        });

        return starterCounts.values()
                .stream()
                .filter(count -> count >= 3)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
