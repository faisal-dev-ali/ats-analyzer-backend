package com.faisal.dev.atsanalyzer.validation;

import com.faisal.dev.atsanalyzer.config.StorageProperties;
import com.faisal.dev.atsanalyzer.exception.FileValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ResumeFileValidator {

    private final StorageProperties storageProperties;

    public void validate(MultipartFile file) {

        validateNotEmpty(file);
        validateFileSize(file);
        validateFileName(file);
        validateFileExtension(file);
        validateContentType(file);
    }

    private void validateNotEmpty(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileValidationException(
                    "Uploaded file is empty"
            );
        }
    }

    private void validateFileSize(MultipartFile file) {

        if (file.getSize() >
                storageProperties.getMaxFileSizeBytes()) {

            throw new FileValidationException(
                    "File size exceeds maximum allowed limit of 5MB"
            );
        }
    }

    private void validateFileName(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                fileName.isBlank() ||
                fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\")) {

            throw new FileValidationException(
                    "Invalid file name"
            );
        }
    }

    private void validateFileExtension(MultipartFile file) {

        String extension = FilenameUtils.getExtension(
                file.getOriginalFilename()
        ).toLowerCase(Locale.ROOT);

        if (!storageProperties.getAllowedExtensions()
                .contains(extension)) {

            throw new FileValidationException(
                    "Only PDF and DOCX files are allowed"
            );
        }
    }

    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null ||
                !storageProperties.getAllowedContentTypes()
                        .contains(contentType)) {

            throw new FileValidationException(
                    "Invalid file content type"
            );
        }
    }
}
