package com.faisal.dev.atsanalyzer.validation;

import com.faisal.dev.atsanalyzer.config.StorageProperties;
import com.faisal.dev.atsanalyzer.exception.FileValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResumeFileValidatorTest {

    private final ResumeFileValidator validator =
            new ResumeFileValidator(buildProperties());

    @Test
    void acceptsConfiguredPdfFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "sample resume".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void rejectsUnsupportedExtension() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "sample resume".getBytes()
        );

        assertThrows(
                FileValidationException.class,
                () -> validator.validate(file)
        );
    }

    private static StorageProperties buildProperties() {

        StorageProperties properties =
                new StorageProperties();

        properties.setUploadDir("uploads/resumes");
        properties.setMaxFileSizeBytes(5 * 1024 * 1024);
        properties.setAllowedExtensions(
                new LinkedHashSet<>(
                        java.util.List.of("pdf", "docx")
                )
        );
        properties.setAllowedContentTypes(
                new LinkedHashSet<>(
                        java.util.List.of(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                )
        );

        return properties;
    }
}
