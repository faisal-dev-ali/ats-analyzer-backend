package com.faisal.dev.atsanalyzer.dto;

import java.util.List;

public record ResumeKeywordOverviewResponse(
        Integer matchedKeywords,
        Integer missingKeywords,
        Integer totalKeywords,
        List<String> matchedSkills,
        List<String> missingSkills
) {
}
