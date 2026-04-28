package com.faisal.dev.atsanalyzer.parser;

import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.exception.ParsingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeParsingService {

    private final List<ResumeTextExtractor> textExtractors;

    private final TextCleaner textCleaner;

    public ParsedResumeContent parse(Resume resume) {

        Path filePath = Path.of(resume.getStoragePath())
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(filePath)) {
            throw new ParsingException(
                    "Uploaded resume file could not be found"
            );
        }

        ResumeTextExtractor extractor =
                textExtractors.stream()
                        .filter(candidate ->
                                candidate.supports(
                                        resume.getFileType(),
                                        resume.getOriginalFileName()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ParsingException(
                                        "Unsupported resume file type"
                                )
                        );

        ExtractedResumeContent extractedContent =
                extractor.extract(filePath);

        String cleanedText = textCleaner.clean(
                extractedContent.extractedText()
        );

        if (cleanedText.isBlank()) {
            throw new ParsingException(
                    "No readable text could be extracted from the resume"
            );
        }

        log.debug(
                "Resume text parsed successfully. resumeId={}, totalCharacters={}",
                resume.getId(),
                cleanedText.length()
        );

        return new ParsedResumeContent(
                extractedContent.extractedText(),
                cleanedText,
                extractedContent.totalPages(),
                calculateWordCount(cleanedText),
                cleanedText.length()
        );
    }

    private int calculateWordCount(String text) {

        return text.trim().split("\\s+").length;
    }
}
