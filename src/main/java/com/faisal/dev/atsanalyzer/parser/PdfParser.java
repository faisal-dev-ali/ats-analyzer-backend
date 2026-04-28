package com.faisal.dev.atsanalyzer.parser;

import com.faisal.dev.atsanalyzer.exception.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
@Slf4j
public class PdfParser implements ResumeTextExtractor {

    private static final String PDF_CONTENT_TYPE =
            "application/pdf";

    @Override
    public boolean supports(
            String contentType,
            String fileName
    ) {

        return PDF_CONTENT_TYPE.equalsIgnoreCase(contentType) ||
                (fileName != null &&
                        fileName.toLowerCase().endsWith(".pdf"));
    }

    @Override
    public ExtractedResumeContent extract(Path filePath) {

        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {

            if (document.isEncrypted()) {

                throw new ParsingException(
                        "Encrypted PDF files are not supported"
                );
            }

            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            String extractedText = pdfTextStripper.getText(document);

            log.info(
                    "PDF text extraction completed successfully. path={}",
                    filePath
            );

            return new ExtractedResumeContent(
                    extractedText,
                    document.getNumberOfPages()
            );

        } catch (IOException ex) {

            log.error("Failed to parse PDF file", ex);

            throw new ParsingException(
                    "Failed to parse PDF file",
                    ex
            );
        }
    }
}
