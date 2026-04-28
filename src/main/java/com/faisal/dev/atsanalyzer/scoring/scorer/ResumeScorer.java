package com.faisal.dev.atsanalyzer.scoring.scorer;

import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;

public interface ResumeScorer {

    ScoreResult calculateScore(
            ScoringContext context
    );
}
