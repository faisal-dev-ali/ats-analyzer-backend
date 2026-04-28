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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(4)
@RequiredArgsConstructor
public class ActionVerbScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        List<String> bulletLines = context.bulletLines();

        if (bulletLines.isEmpty()) {
            return ScoreResult.builder()
                    .category(ScoreCategory.ACTION_VERBS)
                    .score(45.0d)
                    .weight(scoringProperties.weightFor(
                            ScoreCategory.ACTION_VERBS
                    ))
                    .explanation(
                            "The resume has few bullet-based achievement statements, so action-oriented writing is hard to evaluate."
                    )
                    .recommendations(List.of(
                            "Rewrite core experience points as bullets that start with decisive action verbs."
                    ))
                    .build();
        }

        List<String> normalizedActionVerbs =
                scoringProperties.normalizedActionVerbs();
        List<String> normalizedWeakActionVerbs =
                scoringProperties.normalizedWeakActionVerbs();

        int strongStarts = 0;
        int weakStarts = 0;
        Set<String> distinctStrongVerbs =
                new LinkedHashSet<>();

        for (String bulletLine : bulletLines) {

            String normalizedLine =
                    TextAnalysisUtils.normalizeText(
                            bulletLine.replaceFirst(
                                    "^\\s*(?:[-*•]|\\d+[.)])\\s*",
                                    ""
                            )
                    );

            String firstToken = normalizedLine
                    .split("\\s+")[0];

            if (normalizedActionVerbs.contains(firstToken)) {
                strongStarts++;
                distinctStrongVerbs.add(firstToken);
            }

            if (normalizedWeakActionVerbs.stream()
                    .anyMatch(normalizedLine::startsWith)) {
                weakStarts++;
            }
        }

        double strongRatio =
                TextAnalysisUtils.ratio(
                        strongStarts,
                        bulletLines.size()
                );

        double varietyScore =
                TextAnalysisUtils.ratio(
                        Math.min(
                                distinctStrongVerbs.size(),
                                6
                        ),
                        6
                ) * 35.0d;

        double score = strongRatio * 55.0d +
                varietyScore +
                Math.max(0.0d,
                        10.0d - weakStarts * 3.0d);

        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();

        if (strongStarts >= Math.max(
                2,
                bulletLines.size() / 2
        )) {
            highlights.add(
                    "Many achievement bullets begin with direct action verbs."
            );
        }

        if (distinctStrongVerbs.size() >= 4) {
            highlights.add(
                    "Verb variety keeps the resume energetic instead of repetitive."
            );
        }

        if (strongRatio < 0.45d) {
            recommendations.add(
                    "Start more bullets with decisive action verbs such as built, led, improved, launched, or delivered."
            );
        }

        if (weakStarts >= 2) {
            recommendations.add(
                    "Replace weak openings like \"responsible for\" or \"helped with\" with stronger verbs plus specific outcomes."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.ACTION_VERBS)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.ACTION_VERBS
                ))
                .explanation(
                        "About " +
                                strongStarts +
                                " of " +
                                bulletLines.size() +
                                " bullets start with strong action verbs, with " +
                                distinctStrongVerbs.size() +
                                " distinct verb stems."
                )
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
