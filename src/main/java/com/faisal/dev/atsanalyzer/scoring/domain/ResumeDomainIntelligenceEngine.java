package com.faisal.dev.atsanalyzer.scoring.domain;

import com.faisal.dev.atsanalyzer.scoring.config.DomainPackDefinition;
import com.faisal.dev.atsanalyzer.scoring.detector.ExperienceExtractor;
import com.faisal.dev.atsanalyzer.scoring.detector.ResumeDomainDetector;
import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityDetector;
import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityLevel;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ResumeDomainIntelligenceEngine {

    private final ResumeDomainDetector resumeDomainDetector;

    private final ExperienceExtractor experienceExtractor;

    private final SeniorityDetector seniorityDetector;

    public ResumeInsights analyze(
            ScoringContext context
    ) {

        int experienceYears =
                experienceExtractor.extractYears(
                        context.cleanedText()
                );

        SeniorityLevel seniorityLevel =
                seniorityDetector.detect(
                        context.normalizedText(),
                        experienceYears
                );

        DomainDetectionResult detectionResult =
                resumeDomainDetector.detect(
                        context.normalizedText()
                );

        DomainPackDefinition domainPack =
                detectionResult.domainPack();

        Set<String> detectedSkills =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        context.normalizedText(),
                        domainPack.allSkillSignals()
                );

        List<String> expectedSkills =
                domainPack.expectedSkillsFor(
                        seniorityLevel
                );

        Set<String> matchedExpectedSkills =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        context.normalizedText(),
                        expectedSkills
                );

        List<String> missingSkills = expectedSkills.stream()
                .filter(expectedSkill ->
                        !containsIgnoreCase(
                                matchedExpectedSkills,
                                expectedSkill
                        )
                )
                .limit(8)
                .toList();

        return new ResumeInsights(
                domainPack.key(),
                domainPack.label(),
                detectionResult.confidence(),
                seniorityLevel,
                experienceYears,
                expectedSkills.stream()
                        .limit(10)
                        .toList(),
                detectedSkills.stream()
                        .limit(12)
                        .toList(),
                missingSkills,
                detectionResult.matchedSignals(),
                buildExplanation(
                        domainPack,
                        detectionResult,
                        seniorityLevel,
                        matchedExpectedSkills.size(),
                        expectedSkills.size(),
                        missingSkills
                )
        );
    }

    private String buildExplanation(
            DomainPackDefinition domainPack,
            DomainDetectionResult detectionResult,
            SeniorityLevel seniorityLevel,
            int matchedExpectedSkills,
            int totalExpectedSkills,
            List<String> missingSkills
    ) {

        if (domainPack.fallback()) {
            return "The resume does not strongly signal one specialized track, so domain guidance is based on a general professional baseline.";
        }

        StringBuilder explanation =
                new StringBuilder(
                        "The resume most closely aligns with "
                                + domainPack.label()
                                + " based on signals such as "
                                + String.join(
                                ", ",
                                detectionResult.matchedSignals()
                        )
                                + "."
                );

        if (seniorityLevel != SeniorityLevel.UNKNOWN) {
            explanation.append(" It reads like a ")
                    .append(seniorityLevel.getLabel())
                    .append(" profile");

            if (totalExpectedSkills > 0) {
                explanation.append(" with ")
                        .append(matchedExpectedSkills)
                        .append(" of ")
                        .append(totalExpectedSkills)
                        .append(" commonly expected signals surfaced explicitly.");
            } else {
                explanation.append(".");
            }
        }

        if (!missingSkills.isEmpty()) {
            explanation.append(" Missing signals are based on common expectations for that level, not mandatory requirements.");
        }

        return explanation.toString();
    }

    private boolean containsIgnoreCase(
            Set<String> values,
            String target
    ) {

        return values.stream()
                .map(TextAnalysisUtils::normalizeText)
                .anyMatch(value ->
                        value.equals(
                                TextAnalysisUtils.normalizeText(
                                        target
                                )
                        )
                );
    }
}
