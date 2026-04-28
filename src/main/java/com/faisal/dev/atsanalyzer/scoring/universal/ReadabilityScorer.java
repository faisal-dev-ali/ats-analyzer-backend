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
import java.util.List;

@Component
@Order(3)
@RequiredArgsConstructor
public class ReadabilityScorer
        implements ResumeScorer {

    private final AtsScoringProperties scoringProperties;

    @Override
    public ScoreResult calculateScore(
            ScoringContext context
    ) {

        int lineCount = Math.max(context.lines().size(), 1);
        int sentenceCount = Math.max(
                context.sentences().size(),
                1
        );

        double averageWordsPerLine =
                TextAnalysisUtils.ratio(
                        context.totalWords(),
                        lineCount
                );

        double averageSentenceLength =
                TextAnalysisUtils.ratio(
                        context.totalWords(),
                        sentenceCount
                );

        double bulletDensity =
                TextAnalysisUtils.ratio(
                        context.bulletLines().size(),
                        lineCount
                );

        int denseLines = TextAnalysisUtils.countLinesLongerThan(
                context.lines(),
                28
        );

        double score = 100.0d;
        List<String> findings = new ArrayList<>();
        List<String> highlights = new ArrayList<>();
        List<String> recommendations =
                new ArrayList<>();

        if (averageWordsPerLine > 18.0d) {
            score -= 12.0d;
            findings.add(
                    "Line length is denser than ideal for quick scanning."
            );
            recommendations.add(
                    "Keep most lines shorter by tightening wording and splitting long bullets."
            );
        } else {
            highlights.add(
                    "Line length supports quick scanning."
            );
        }

        if (averageSentenceLength > 26.0d) {
            score -= 15.0d;
            findings.add(
                    "Sentence length is heavy for resume-style reading."
            );
            recommendations.add(
                    "Shorten multi-clause bullet points so each line lands one main achievement."
            );
        }

        if (denseLines > 4) {
            score -= 15.0d;
            findings.add(
                    "Several bullets or lines are too long."
            );
            recommendations.add(
                    "Cap most bullets at one to two lines to improve readability."
            );
        }

        if (lineCount > 8 && bulletDensity < 0.18d) {
            score -= 12.0d;
            findings.add(
                    "The document relies on paragraphs more than bullets."
            );
            recommendations.add(
                    "Use bullets for achievements so both recruiters and ATS systems can scan the resume faster."
            );
        } else if (bulletDensity >= 0.18d) {
            highlights.add(
                    "Bullet usage makes the resume easy to scan."
            );
        }

        return ScoreResult.builder()
                .category(ScoreCategory.READABILITY)
                .score(TextAnalysisUtils.clampAndRound(score))
                .weight(scoringProperties.weightFor(
                        ScoreCategory.READABILITY
                ))
                .explanation(findings.isEmpty()
                        ? "The resume is easy to scan with concise lines and readable bullet structure."
                        : String.join(" ", findings))
                .highlights(highlights)
                .recommendations(recommendations)
                .build();
    }
}
