package com.faisal.dev.atsanalyzer.scoring.detector;

import com.faisal.dev.atsanalyzer.scoring.utils.TextAnalysisUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeniorityDetector {

    private static final List<String> ENTRY_KEYWORDS =
            List.of(
                    "intern",
                    "junior",
                    "fresher",
                    "trainee",
                    "entry level",
                    "associate"
            );

    private static final List<String> SENIOR_KEYWORDS =
            List.of(
                    "senior",
                    "sr",
                    "staff",
                    "principal"
            );

    private static final List<String> LEAD_KEYWORDS =
            List.of(
                    "lead",
                    "head",
                    "director",
                    "vice president",
                    "vp",
                    "chief",
                    "architect"
            );

    public SeniorityLevel detect(
            String normalizedText,
            int experienceYears
    ) {

        if (TextAnalysisUtils.containsAnyTerm(
                normalizedText,
                LEAD_KEYWORDS
        )) {
            return SeniorityLevel.LEAD;
        }

        if (TextAnalysisUtils.containsAnyTerm(
                normalizedText,
                ENTRY_KEYWORDS
        )) {
            return SeniorityLevel.ENTRY;
        }

        if (TextAnalysisUtils.containsAnyTerm(
                normalizedText,
                SENIOR_KEYWORDS
        )) {
            return SeniorityLevel.SENIOR;
        }

        if (experienceYears >= 9) {
            return SeniorityLevel.LEAD;
        }

        if (experienceYears >= 6) {
            return SeniorityLevel.SENIOR;
        }

        if (experienceYears >= 2) {
            return SeniorityLevel.MID;
        }

        if (experienceYears >= 0 &&
                !normalizedText.isBlank()) {
            return SeniorityLevel.ENTRY;
        }

        return SeniorityLevel.UNKNOWN;
    }
}
