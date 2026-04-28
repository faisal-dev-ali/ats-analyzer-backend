package com.faisal.dev.atsanalyzer.dto;

public record ResumeScoreCardResponse(
        String id,
        DashboardCategory category,
        String title,
        Double score,
        Integer weight,
        String description,
        String status,
        DashboardColor color,
        DashboardIcon icon
) {
}
