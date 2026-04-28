package com.faisal.dev.atsanalyzer.scoring.suggestion;

import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.entity.Severity;
import com.faisal.dev.atsanalyzer.entity.Suggestion;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResumeSuggestionGenerator {

    public List<Suggestion> generate(
            Resume resume,
            DetailedScoreResult detailedScoreResult
    ) {

        List<Suggestion> suggestions = new ArrayList<>();

        for (ScoreResult scoreResult :
                detailedScoreResult.getCategoryScores()) {

            scoreResult.getRecommendations()
                    .stream()
                    .limit(2)
                    .map(message ->
                            Suggestion.builder()
                                    .resume(resume)
                                    .category(
                                            scoreResult.getCategory()
                                                    .getSuggestionCategory().name()
                                    )
                                    .severity(
                                            determineSeverity(
                                                    scoreResult.getScore()
                                            ).name()
                                    )
                                    .message(message)
                                    .createdAt(
                                            LocalDateTime.now()
                                    )
                                    .build()
                    )
                    .forEach(suggestions::add);
        }

        if (suggestions.isEmpty()) {
            suggestions.add(
                    Suggestion.builder()
                            .resume(resume)
                            .category(
                                    detailedScoreResult.getCategoryScores()
                                            .get(0)
                                            .getCategory()
                                            .getSuggestionCategory().name()
                            )
                            .severity(Severity.LOW.name())
                            .message(
                                    "The resume is structurally strong. Keep tailoring impact, wording, and role-specific terminology for each application."
                            )
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        return suggestions.stream()
                .limit(8)
                .toList();
    }

    private Severity determineSeverity(Double score) {

        if (score < 50.0d) {
            return Severity.HIGH;
        }

        if (score < 75.0d) {
            return Severity.MEDIUM;
        }

        return Severity.LOW;
    }
}
