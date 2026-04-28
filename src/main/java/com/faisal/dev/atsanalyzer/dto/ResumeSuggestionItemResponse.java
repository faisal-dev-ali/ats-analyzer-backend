package com.faisal.dev.atsanalyzer.dto;

public record ResumeSuggestionItemResponse(
        String id,
        DashboardCategory category,
        DashboardSeverity severity,
        String title,
        String message,
        DashboardIcon icon,
        DashboardColor color
) {
}
