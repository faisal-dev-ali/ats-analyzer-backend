package com.faisal.dev.atsanalyzer.scoring.config;

import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityLevel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public record DomainPackDefinition(
        String key,
        String label,
        boolean fallback,
        List<String> roleKeywords,
        List<String> keywords,
        List<String> tools,
        List<String> terminology,
        List<String> baselineExpectations,
        Map<String, List<String>> seniorityExpectations
) {

    public DomainPackDefinition {

        roleKeywords = copyOf(roleKeywords);
        keywords = copyOf(keywords);
        tools = copyOf(tools);
        terminology = copyOf(terminology);
        baselineExpectations = copyOf(baselineExpectations);
        seniorityExpectations = copyMap(seniorityExpectations);
    }

    public List<String> allDetectionSignals() {

        LinkedHashSet<String> terms = new LinkedHashSet<>();
        terms.addAll(roleKeywords);
        terms.addAll(keywords);
        terms.addAll(tools);
        terms.addAll(terminology);
        return List.copyOf(terms);
    }

    public List<String> expectedSkillsFor(
            SeniorityLevel seniorityLevel
    ) {

        LinkedHashSet<String> expectedSkills =
                new LinkedHashSet<>(baselineExpectations);

        switch (seniorityLevel) {
            case ENTRY -> expectedSkills.addAll(
                    expectationsFor("ENTRY")
            );
            case MID -> expectedSkills.addAll(
                    expectationsFor("MID")
            );
            case SENIOR -> expectedSkills.addAll(
                    expectationsFor("SENIOR")
            );
            case LEAD -> {
                expectedSkills.addAll(
                        expectationsFor("SENIOR")
                );
                expectedSkills.addAll(
                        expectationsFor("LEAD")
                );
            }
            case UNKNOWN -> {
            }
        }

        return List.copyOf(expectedSkills);
    }

    public List<String> allSkillSignals() {

        LinkedHashSet<String> terms = new LinkedHashSet<>();
        terms.addAll(baselineExpectations);
        seniorityExpectations.values()
                .forEach(terms::addAll);
        terms.addAll(tools);
        terms.addAll(keywords);
        terms.addAll(terminology);
        return List.copyOf(terms);
    }

    private List<String> expectationsFor(
            String levelKey
    ) {

        return seniorityExpectations.getOrDefault(
                levelKey,
                List.of()
        );
    }

    private static List<String> copyOf(
            List<String> values
    ) {

        return values == null ? List.of() : List.copyOf(values);
    }

    private static Map<String, List<String>> copyMap(
            Map<String, List<String>> values
    ) {

        if (values == null) {
            return Map.of();
        }

        Map<String, List<String>> copy =
                new LinkedHashMap<>();

        values.forEach((key, list) ->
                copy.put(key, copyOf(list))
        );

        return Map.copyOf(copy);
    }
}
