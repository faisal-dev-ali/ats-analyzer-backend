package com.faisal.dev.atsanalyzer.parser;

import java.nio.file.Path;

public interface ResumeTextExtractor {

    boolean supports(String contentType, String fileName);

    ExtractedResumeContent extract(Path filePath);
}
