package com.faisal.dev.atsanalyzer.scoring.result;

import com.faisal.dev.atsanalyzer.scoring.domain.ResumeInsights;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DetailedScoreResult {

    private Double overallScore;

    private Double universalScore;

    private Double domainRelevanceScore;

    private List<ScoreResult> categoryScores;

    private List<String> strengths;

    private List<String> improvements;

    private ResumeInsights resumeInsights;
}
