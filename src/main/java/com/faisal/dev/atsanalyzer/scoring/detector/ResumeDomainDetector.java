package com.faisal.dev.atsanalyzer.scoring.detector;

import com.faisal.dev.atsanalyzer.scoring.config.DomainPackDefinition;
import com.faisal.dev.atsanalyzer.scoring.domain.DomainDetectionResult;
import com.faisal.dev.atsanalyzer.scoring.domain.DomainPackRegistry;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeDomainDetector {

    private final DomainPackRegistry domainPackRegistry;

    public DomainDetectionResult detect(
            String normalizedText
    ) {

        List<DomainMatch> rankedMatches =
                domainPackRegistry.activePacks()
                        .stream()
                        .map(pack ->
                                scoreDomain(
                                        normalizedText,
                                        pack
                                )
                        )
                        .sorted(Comparator.comparingDouble(
                                DomainMatch::rawScore
                        ).reversed())
                        .toList();

        DomainMatch bestMatch = rankedMatches.isEmpty()
                ? null
                : rankedMatches.get(0);

        if (bestMatch == null ||
                bestMatch.rawScore() < 4.0d) {
            return new DomainDetectionResult(
                    domainPackRegistry.fallbackPack(),
                    bestMatch == null
                            ? 0.0d
                            : TextAnalysisUtils.clampAndRound(
                                    Math.min(
                                            0.35d,
                                            bestMatch.rawScore() / 10.0d
                                    ) * 100.0d
                            ) / 100.0d,
                    bestMatch == null
                            ? List.of()
                            : bestMatch.matchedSignals()
            );
        }

        double runnerUpScore = rankedMatches.size() > 1
                ? rankedMatches.get(1).rawScore()
                : 0.0d;

        double confidence = Math.min(
                1.0d,
                (bestMatch.rawScore() / 18.0d) * 0.75d +
                        Math.min(
                                1.0d,
                                Math.max(
                                        0.0d,
                                        bestMatch.rawScore() -
                                                runnerUpScore
                                ) / 6.0d
                        ) * 0.25d
        );

        return new DomainDetectionResult(
                bestMatch.domainPack(),
                TextAnalysisUtils.clampAndRound(
                        confidence * 100.0d
                ) / 100.0d,
                bestMatch.matchedSignals()
        );
    }

    private DomainMatch scoreDomain(
            String normalizedText,
            DomainPackDefinition domainPack
    ) {

        List<String> matchedRoleKeywords =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        normalizedText,
                        domainPack.roleKeywords()
                )
                        .stream()
                        .toList();

        List<String> matchedKeywords =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        normalizedText,
                        domainPack.keywords()
                )
                        .stream()
                        .toList();

        List<String> matchedTools =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        normalizedText,
                        domainPack.tools()
                )
                        .stream()
                        .toList();

        List<String> matchedTerminology =
                TextAnalysisUtils.findMatchedTermsPreservingCase(
                        normalizedText,
                        domainPack.terminology()
                )
                        .stream()
                        .toList();

        double rawScore = matchedRoleKeywords.size() * 4.0d +
                matchedTools.size() * 2.5d +
                matchedKeywords.size() * 2.0d +
                matchedTerminology.size() * 1.5d;

        return new DomainMatch(
                domainPack,
                rawScore,
                List.copyOf(
                        java.util.stream.Stream.of(
                                        matchedRoleKeywords,
                                        matchedTools,
                                        matchedKeywords,
                                        matchedTerminology
                                )
                                .flatMap(List::stream)
                                .distinct()
                                .limit(8)
                                .toList()
                )
        );
    }

    private record DomainMatch(
            DomainPackDefinition domainPack,
            double rawScore,
            List<String> matchedSignals
    ) {
    }
}
