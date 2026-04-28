package com.faisal.dev.atsanalyzer.scoring.utils;

import java.util.regex.Pattern;

public final class RegexUtils {

    private RegexUtils() {
    }

    public static final Pattern BULLET_PATTERN =
            Pattern.compile(
                    "^\\s*(?:[-*•]|\\d+[.)])\\s+.+$"
            );

    public static final Pattern SPECIAL_CHARACTER_PATTERN =
            Pattern.compile(
                    "[^\\p{L}\\p{N}\\s.,;:()/%+&\\-]"
            );

    public static final Pattern METRIC_PATTERN =
            Pattern.compile(
                    "\\b(?:\\d+(?:\\.\\d+)?%|\\$\\d[\\d,.]*|\\d+(?:\\.\\d+)?x|\\d[\\d,.]*\\s?(?:ms|sec|secs|seconds|minutes|hours|days|users|customers|clients|records|requests|transactions|downloads|projects|features))\\b",
                    Pattern.CASE_INSENSITIVE
            );

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b"
            );

    public static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "\\b(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{3}\\)?[\\s.-]?)\\d{3}[\\s.-]?\\d{4}\\b"
            );

    public static final Pattern LINKEDIN_PATTERN =
            Pattern.compile(
                    "\\b(?:linkedin\\.com|linkedin)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    public static final Pattern YEAR_PATTERN =
            Pattern.compile(
                    "\\b(19|20)\\d{2}\\b"
            );

    public static final Pattern EXPLICIT_EXPERIENCE_PATTERN =
            Pattern.compile(
                    "\\b(\\d{1,2})\\+?\\s+(?:years?|yrs?)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    public static final Pattern YEAR_RANGE_PATTERN =
            Pattern.compile(
                    "\\b((?:19|20)\\d{2})\\s*(?:-|to|–|—)\\s*(present|current|now|(?:19|20)\\d{2})\\b",
                    Pattern.CASE_INSENSITIVE
            );
}
