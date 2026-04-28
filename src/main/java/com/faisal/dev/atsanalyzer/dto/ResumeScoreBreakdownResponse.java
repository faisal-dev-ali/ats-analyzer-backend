package com.faisal.dev.atsanalyzer.dto;

import com.faisal.dev.atsanalyzer.scoring.constants.ScoreLayer;

import java.util.List;

public record ResumeScoreBreakdownResponse(
        DashboardCategory category,
        String title,
        ScoreLayer layer,
        Double score,
        Integer weight,
        String status,
        DashboardColor color,
        DashboardIcon icon,
        String explanation,
        List<String> highlights,
        List<String> recommendations
) {
}
