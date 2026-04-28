package com.faisal.dev.atsanalyzer.scoring.universal;

import com.faisal.dev.atsanalyzer.scoring.config.AtsScoringProperties;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityLevel;
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
@Order(6)
@RequiredArgsConstructor
public class ResumeLengthScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        SeniorityLevel seniorityLevel = context.resumeInsights()
                .seniorityLevel();

        int words = context.totalWords();
        Range idealRange = idealRangeFor(seniorityLevel);

        double score = 100.0d;
        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();
        List<String> findings = new ArrayList<>();

        if (words < idealRange.min()) {
            score -= Math.min(
                    35.0d,
                    (idealRange.min() - words) * 0.08d
            );
            findings.add(
                    "The resume looks light for the inferred experience level."
            );
            recommendations.add(
                    "Add a few more concrete bullets, tools, or measurable outcomes so the resume feels more complete."
            );
        } else if (words > idealRange.max()) {
            score -= Math.min(
                    35.0d,
                    (words - idealRange.max()) * 0.05d
            );
            findings.add(
                    "The resume is longer than ideal for fast ATS and recruiter scanning."
            );
            recommendations.add(
                    "Trim older or lower-impact details so the strongest recent achievements stand out first."
            );
        } else {
            highlights.add(
                    "Resume length looks appropriate for the inferred experience level."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.RESUME_LENGTH)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.RESUME_LENGTH
                ))
                .explanation(findings.isEmpty()
                        ? "The resume length is in a healthy range for the inferred career stage."
                        : String.join(" ", findings))
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }

    private Range idealRangeFor(
            SeniorityLevel seniorityLevel
    ) {

        return switch (seniorityLevel) {
            case ENTRY -> new Range(250, 575);
            case MID -> new Range(400, 750);
            case SENIOR -> new Range(500, 900);
            case LEAD -> new Range(550, 1000);
            case UNKNOWN -> new Range(300, 800);
        };
    }

    private record Range(
            int min,
            int max
    ) {
    }
}
