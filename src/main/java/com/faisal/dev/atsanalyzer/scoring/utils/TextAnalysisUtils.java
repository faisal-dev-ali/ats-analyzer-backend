package com.faisal.dev.atsanalyzer.scoring.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextAnalysisUtils {

    private TextAnalysisUtils() {
    }

    public static List<String> nonEmptyLines(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    public static List<String> bulletLines(List<String> lines) {

        return lines.stream()
                .filter(line ->
                        RegexUtils.BULLET_PATTERN
                                .matcher(line)
                                .matches()
                )
                .toList();
    }

    public static List<String> sentences(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return Pattern.compile("[.!?]+\\s+")
                .splitAsStream(text)
                .map(String::trim)
                .filter(sentence -> !sentence.isBlank())
                .toList();
    }

    public static String normalizeText(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        return text.toLowerCase(Locale.ROOT)
                .replace('\u00A0', ' ')
                .replaceAll("[^a-z0-9+#./\\-\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static Set<String> findMatchedTerms(
            String normalizedText,
            Collection<String> terms
    ) {

        Set<String> matches = new LinkedHashSet<>();

        for (String term : terms) {

            String normalizedTerm = normalizeText(term);

            if (!normalizedTerm.isBlank() &&
                    containsTerm(
                            normalizedText,
                            normalizedTerm
                    )) {

                matches.add(normalizedTerm);
            }
        }

        return matches;
    }

    public static Set<String> findMatchedTermsPreservingCase(
            String normalizedText,
            Collection<String> terms
    ) {

        Set<String> matches = new LinkedHashSet<>();

        for (String term : terms) {

            String normalizedTerm = normalizeText(term);

            if (!normalizedTerm.isBlank() &&
                    containsTerm(
                            normalizedText,
                            normalizedTerm
                    )) {

                matches.add(term.trim());
            }
        }

        return matches;
    }

    public static int countTermOccurrences(
            String normalizedText,
            Collection<String> terms
    ) {

        int totalOccurrences = 0;

        for (String term : terms) {

            String normalizedTerm = normalizeText(term);

            if (normalizedTerm.isBlank()) {
                continue;
            }

            Matcher matcher = buildTermPattern(
                    normalizedTerm
            ).matcher(normalizedText);

            while (matcher.find()) {
                totalOccurrences++;
            }
        }

        return totalOccurrences;
    }

    public static boolean containsAnyTerm(
            String normalizedText,
            Collection<String> terms
    ) {

        return terms.stream()
                .map(TextAnalysisUtils::normalizeText)
                .anyMatch(term ->
                        containsTerm(
                                normalizedText,
                                term
                        )
                );
    }

    public static LinkedHashMap<String, Integer> sectionPositions(
            String normalizedText,
            Map<String, List<String>> sections
    ) {

        LinkedHashMap<String, Integer> positions =
                new LinkedHashMap<>();

        sections.forEach((sectionKey, aliases) -> {
            int earliestMatch = Integer.MAX_VALUE;

            for (String alias : aliases) {

                String normalizedAlias =
                        normalizeText(alias);

                Matcher matcher = buildTermPattern(
                        normalizedAlias
                ).matcher(normalizedText);

                if (matcher.find()) {
                    earliestMatch = Math.min(
                            earliestMatch,
                            matcher.start()
                    );
                }
            }

            if (earliestMatch != Integer.MAX_VALUE) {
                positions.put(sectionKey, earliestMatch);
            }
        });

        return positions;
    }

    public static long countPatternMatches(
            String text,
            Pattern pattern
    ) {

        return pattern.matcher(text).results().count();
    }

    public static boolean hasContactInfo(String text) {

        return RegexUtils.EMAIL_PATTERN.matcher(text).find() &&
                (RegexUtils.PHONE_PATTERN.matcher(text).find() ||
                        RegexUtils.LINKEDIN_PATTERN
                                .matcher(text)
                                .find());
    }

    public static int countLinesLongerThan(
            List<String> lines,
            int maxWords
    ) {

        int count = 0;

        for (String line : lines) {
            if (wordCount(line) > maxWords) {
                count++;
            }
        }

        return count;
    }

    public static int wordCount(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    public static double ratio(int numerator, int denominator) {

        if (denominator <= 0) {
            return 0.0d;
        }

        return (double) numerator / denominator;
    }

    public static double clampAndRound(double score) {

        double normalizedScore = Math.max(
                0.0d,
                Math.min(100.0d, score)
        );

        return BigDecimal.valueOf(normalizedScore)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static List<String> limitTo(
            Collection<String> items,
            int limit
    ) {

        return new ArrayList<>(items).stream()
                .limit(limit)
                .toList();
    }

    private static boolean containsTerm(
            String normalizedText,
            String normalizedTerm
    ) {

        if (normalizedText == null || normalizedText.isBlank()) {
            return false;
        }

        return buildTermPattern(normalizedTerm)
                .matcher(normalizedText)
                .find();
    }

    private static Pattern buildTermPattern(
            String normalizedTerm
    ) {

        return Pattern.compile(
                "(?<![a-z0-9+#./-])" +
                        Pattern.quote(normalizedTerm) +
                        "(?![a-z0-9+#./-])"
        );
    }
}
