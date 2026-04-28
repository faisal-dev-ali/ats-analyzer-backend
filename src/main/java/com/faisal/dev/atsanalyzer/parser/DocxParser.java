package com.faisal.dev.atsanalyzer.parser;

import com.faisal.dev.atsanalyzer.exception.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class DocxParser implements ResumeTextExtractor {

    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public boolean supports(
            String contentType,
            String fileName
    ) {

        return DOCX_CONTENT_TYPE.equalsIgnoreCase(contentType) ||
                (fileName != null &&
                        fileName.toLowerCase().endsWith(".docx"));
    }

    @Override
    public ExtractedResumeContent extract(Path filePath) {

        try (
                InputStream inputStream =
                        Files.newInputStream(filePath);
                XWPFDocument document =
                        new XWPFDocument(inputStream);
                XWPFWordExtractor extractor =
                        new XWPFWordExtractor(document)
        ) {

            String extractedText = extractor.getText();

            log.info(
                    "DOCX text extraction completed successfully. path={}",
                    filePath
            );

            int totalPages = document.getProperties()
                    .getExtendedProperties()
                    .getUnderlyingProperties()
                    .getPages();

            return new ExtractedResumeContent(
                    extractedText,
                    totalPages > 0 ? totalPages : null
            );

        } catch (IOException ex) {

            log.error("Failed to parse DOCX file", ex);

            throw new ParsingException(
                    "Failed to parse DOCX file",
                    ex
            );
        }
    }
}
