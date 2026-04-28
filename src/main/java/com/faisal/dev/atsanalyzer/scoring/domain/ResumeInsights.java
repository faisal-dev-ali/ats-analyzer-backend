package com.faisal.dev.atsanalyzer.scoring.domain;

import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityLevel;

import java.util.List;

public record ResumeInsights(
        String detectedDomainKey,
        String detectedDomainLabel,
        double domainConfidence,
        SeniorityLevel seniorityLevel,
        int inferredExperienceYears,
        List<String> expectedSkills,
        List<String> detectedSkills,
        List<String> missingSkills,
        List<String> matchedSignals,
        String explanation
) {

    public static ResumeInsights unknown() {

        return new ResumeInsights(
                "general-professional",
                "General Professional",
                0.0d,
                SeniorityLevel.UNKNOWN,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "No clear domain signature was detected, so the resume is being treated as a general professional profile."
        );
    }
}
