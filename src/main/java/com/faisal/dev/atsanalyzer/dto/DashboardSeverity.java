package com.faisal.dev.atsanalyzer.dto;

public enum DashboardSeverity {

    HIGH,
    MEDIUM,
    LOW;

    public static DashboardSeverity fromValue(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return LOW;
        }

        try {
            return DashboardSeverity.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            return LOW;
        }
    }
}
