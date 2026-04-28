package com.faisal.dev.atsanalyzer.dto;

public record ResumeHighlightResponse(
        String id,
        DashboardCategory category,
        String message,
        DashboardColor color,
        DashboardIcon icon
) {
}
