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
@Order(2)
@RequiredArgsConstructor
public class MetricsImpactScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        int metricMatches = (int)
                TextAnalysisUtils.countPatternMatches(
                        context.cleanedText(),
                        RegexUtils.METRIC_PATTERN
                );

        int metricBulletCount = (int) context.bulletLines()
                .stream()
                .filter(line ->
                        RegexUtils.METRIC_PATTERN
                                .matcher(line)
                                .find()
                )
                .count();

        double score = 35.0d +
                Math.min(metricMatches, 8) * 5.5d +
                Math.min(metricBulletCount, 4) * 5.25d;

        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();

        if (metricMatches >= 3) {
            highlights.add(
                    "The resume includes quantified outcomes instead of generic responsibility statements."
            );
        }

        if (metricBulletCount >= 2) {
            highlights.add(
                    "Multiple bullet points contain measurable business or professional impact."
            );
        }

        if (metricMatches < 2) {
            recommendations.add(
                    "Add measurable outcomes to recent bullet points using percentages, revenue, scale, speed, volume, or throughput."
            );
        }

        if (!context.bulletLines().isEmpty() &&
                metricBulletCount == 0) {
            score -= 10.0d;
            recommendations.add(
                    "Turn at least a few responsibility bullets into result-oriented bullets with numbers."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.IMPACT_METRICS)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.IMPACT_METRICS
                ))
                .explanation(
                        metricMatches > 0
                                ? "Found " + metricMatches + " measurable impact signals across the resume, including " + metricBulletCount + " bullet points with quantifiable outcomes."
                                : "The resume does not currently show measurable impact metrics."
                )
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
