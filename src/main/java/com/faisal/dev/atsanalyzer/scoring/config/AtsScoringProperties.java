package com.faisal.dev.atsanalyzer.scoring.config;

import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "ats.scoring")
@Getter
@Setter
public class AtsScoringProperties {

    private Map<String, Double> weights =
            new LinkedHashMap<>();

    private Map<String, List<String>> standardSections =
            new LinkedHashMap<>();

    private List<String> actionVerbs = new ArrayList<>();

    private List<String> weakActionVerbs =
            new ArrayList<>();

    private List<String> vaguePhrases = new ArrayList<>();

    public double weightFor(ScoreCategory category) {

        return weights.getOrDefault(category.getKey(), 0.0d);
    }

    public List<String> sectionTerms(String sectionKey) {

        return standardSections.getOrDefault(
                sectionKey,
                List.of()
        );
    }

    public List<String> normalizedActionVerbs() {

        return actionVerbs.stream()
                .map(TextAnalysisUtils::normalizeText)
                .toList();
    }

    public List<String> normalizedWeakActionVerbs() {

        return weakActionVerbs.stream()
                .map(TextAnalysisUtils::normalizeText)
                .toList();
    }

    public List<String> normalizedVaguePhrases() {

        return vaguePhrases.stream()
                .map(TextAnalysisUtils::normalizeText)
                .toList();
    }
}
