package com.faisal.dev.atsanalyzer.scoring.detector;

public enum SeniorityLevel {

    UNKNOWN("Unknown", 0),
    ENTRY("Entry Level", 1),
    MID("Mid Level", 2),
    SENIOR("Senior", 3),
    LEAD("Lead", 4);

    private final String label;

    private final int rank;

    SeniorityLevel(
            String label,
            int rank
    ) {

        this.label = label;
        this.rank = rank;
    }

    public String getLabel() {

        return label;
    }

    public boolean isAtLeast(
            SeniorityLevel other
    ) {

        return rank >= other.rank;
    }
}
