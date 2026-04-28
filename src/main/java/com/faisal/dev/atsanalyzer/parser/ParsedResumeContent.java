package com.faisal.dev.atsanalyzer.parser;

public record ParsedResumeContent(
        String extractedText,
        String cleanedText,
        Integer totalPages,
        Integer totalWords,
        Integer totalCharacters
) {
}
