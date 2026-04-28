package com.faisal.dev.atsanalyzer.scoring.detector;

import com.faisal.dev.atsanalyzer.scoring.utils.RegexUtils;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.regex.Matcher;

@Component
public class ExperienceExtractor {

    public int extractYears(
            String text
    ) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        int explicitYears = extractExplicitYears(text);

        if (explicitYears > 0) {
            return explicitYears;
        }

        return extractFromYearRanges(text);
    }

    private int extractExplicitYears(
            String text
    ) {

        Matcher matcher = RegexUtils.EXPLICIT_EXPERIENCE_PATTERN
                .matcher(text);
        int maxYears = 0;

        while (matcher.find()) {
            maxYears = Math.max(
                    maxYears,
                    Integer.parseInt(
                            matcher.group(1)
                    )
            );
        }

        return maxYears;
    }

    private int extractFromYearRanges(
            String text
    ) {

        Matcher matcher = RegexUtils.YEAR_RANGE_PATTERN
                .matcher(text);
        int maxYears = 0;

        while (matcher.find()) {

            int startYear = Integer.parseInt(
                    matcher.group(1)
            );
            String endToken = matcher.group(2);
            int endYear = isCurrent(endToken)
                    ? Year.now().getValue()
                    : Integer.parseInt(endToken);

            if (endYear >= startYear &&
                    endYear - startYear <= 40) {
                maxYears = Math.max(
                        maxYears,
                        endYear - startYear
                );
            }
        }

        return maxYears;
    }

    private boolean isCurrent(
            String token
    ) {

        return "present".equalsIgnoreCase(token) ||
                "current".equalsIgnoreCase(token) ||
                "now".equalsIgnoreCase(token);
    }
}
