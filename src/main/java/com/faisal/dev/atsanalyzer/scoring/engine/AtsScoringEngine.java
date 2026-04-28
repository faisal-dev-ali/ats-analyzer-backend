package com.faisal.dev.atsanalyzer.scoring.engine;

import com.faisal.dev.atsanalyzer.scoring.constants.ScoreLayer;
import com.faisal.dev.atsanalyzer.scoring.domain.ResumeDomainIntelligenceEngine;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import com.faisal.dev.atsanalyzer.scoring.scorer.ResumeScorer;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AtsScoringEngine {

    private final ResumeDomainIntelligenceEngine
            resumeDomainIntelligenceEngine;

    private final List<ResumeScorer> scorers;

    public DetailedScoreResult calculateScores(
            ScoringContext context
    ) {

        ScoringContext enrichedContext =
                context.withResumeInsights(
                        resumeDomainIntelligenceEngine
                                .analyze(context)
                );

        List<ScoreResult> results = scorers.stream()
                .map(scorer ->
                        scorer.calculateScore(
                                enrichedContext
                        )
                )
                .toList();

        return DetailedScoreResult.builder()
                .overallScore(
                        TextAnalysisUtils.clampAndRound(
                                weightedAverage(results)
                        )
                )
                .universalScore(
                        TextAnalysisUtils.clampAndRound(
                                weightedAverageByLayer(
                                        results,
                                        ScoreLayer.UNIVERSAL
                                )
                        )
                )
                .domainRelevanceScore(
                        TextAnalysisUtils.clampAndRound(
                                weightedAverageByLayer(
                                        results,
                                        ScoreLayer.DOMAIN
                                )
                        )
                )
                .categoryScores(results)
                .strengths(
                        results.stream()
                                .flatMap(result ->
                                        result.getHighlights()
                                                .stream()
                                )
                                .distinct()
                                .toList()
                )
                .improvements(
                        results.stream()
                                .flatMap(result ->
                                        result.getRecommendations()
                                                .stream()
                                )
                                .distinct()
                                .toList()
                )
                .resumeInsights(
                        enrichedContext.resumeInsights()
                )
                .build();
    }

    private double weightedAverage(
            List<ScoreResult> results
    ) {

        double totalWeight = results.stream()
                .mapToDouble(ScoreResult::getWeight)
                .sum();

        if (totalWeight <= 0.0d) {
            return 0.0d;
        }

        double weightedScore = results.stream()
                .mapToDouble(result ->
                        result.getScore() *
                                result.getWeight()
                )
                .sum();

        return weightedScore / totalWeight;
    }

    private double weightedAverageByLayer(
            List<ScoreResult> results,
            ScoreLayer scoreLayer
    ) {

        return weightedAverage(
                results.stream()
                        .filter(result ->
                                result.getCategory()
                                        .getLayer() ==
                                        scoreLayer
                        )
                        .toList()
        );
    }
}
