package com.faisal.dev.atsanalyzer.scoring.engine;

import com.faisal.dev.atsanalyzer.scoring.domain.ResumeInsights;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;

import java.util.List;

public record ScoringContext(
        String rawText,
        String cleanedText,
        String normalizedText,
        List<String> lines,
        List<String> bulletLines,
        List<String> sentences,
        Integer totalWords,
        Integer totalCharacters,
        ResumeInsights resumeInsights
) {

    public static ScoringContext of(
            String rawText,
            String cleanedText,
            Integer totalWords,
            Integer totalCharacters
    ) {

        List<String> lines =
                TextAnalysisUtils.nonEmptyLines(cleanedText);

        return new ScoringContext(
                rawText,
                cleanedText,
                TextAnalysisUtils.normalizeText(cleanedText),
                lines,
                TextAnalysisUtils.bulletLines(lines),
                TextAnalysisUtils.sentences(cleanedText),
                totalWords == null ? 0 : totalWords,
                totalCharacters == null ? 0 : totalCharacters,
                ResumeInsights.unknown()
        );
    }

    public ScoringContext withResumeInsights(
            ResumeInsights insights
    ) {

        return new ScoringContext(
                rawText,
                cleanedText,
                normalizedText,
                lines,
                bulletLines,
                sentences,
                totalWords,
                totalCharacters,
                insights
        );
    }
}
